import { DeleteOutlined, EyeOutlined, ReloadOutlined, SearchOutlined } from '@ant-design/icons';
import { ProCard } from '@ant-design/pro-components';
import { App, Button, Descriptions, Input, Modal, Popconfirm, Select, Space, Table, Tag } from 'antd';
import type { ColumnsType, TablePaginationConfig } from 'antd/es/table';
import { useCallback, useEffect, useState } from 'react';
import { EmptyState, ErrorState, LoadingState } from '@/components/state';
import { getCurrentUserPermissions } from '@/services/auth/authService';
import { getApiErrorMessage } from '@/services/http/apiClient';
import type { PageResult } from '@/services/master/customerService';
import {
  cancelSalesOrder,
  deleteSalesOrder,
  getSalesOrderDetail,
  pageSalesOrders,
  resubmitSalesOrder,
  submitSalesOrder,
  type SalesOrderLineRecord,
  type SalesOrderRecord,
} from '@/services/order/salesOrderService';
import { getAuthSession, setAuthSession } from '@/shared/storage/authSessionStorage';

const DEFAULT_PAGE_SIZE = 10;
const INITIAL_PAGE = 1;
const DATE_FORMAT_PATTERN = /^\d{4}-\d{2}-\d{2}$/;
const DATE_FORMAT_ERROR_MESSAGE = '订单日期格式必须为 YYYY-MM-DD';

const statusOptions = [
  { label: '全部状态', value: '' },
  { label: '草稿', value: 'DRAFT' },
  { label: '待审批', value: 'PENDING_APPROVAL' },
  { label: '已驳回', value: 'REJECTED' },
  { label: '已确认', value: 'CONFIRMED' },
  { label: '生产中', value: 'PRODUCING' },
  { label: '备货完成', value: 'READY' },
  { label: '已发货', value: 'SHIPPED' },
  { label: '已完成', value: 'COMPLETED' },
  { label: '已取消', value: 'CANCELLED' },
];

const statusColorMap: Record<string, string> = {
  CANCELLED: 'default',
  COMPLETED: 'green',
  CONFIRMED: 'blue',
  DRAFT: 'default',
  PENDING_APPROVAL: 'gold',
  PRODUCING: 'purple',
  READY: 'cyan',
  REJECTED: 'red',
  SHIPPED: 'geekblue',
};

interface SalesOrderActions {
  canCancel: boolean;
  canDelete: boolean;
  canResubmit: boolean;
  canSubmit: boolean;
}

export function SalesOrderListPage() {
  const { message } = App.useApp();
  const [orderNoInput, setOrderNoInput] = useState('');
  const [orderNo, setOrderNo] = useState('');
  const [status, setStatus] = useState('');
  const [orderDateStartInput, setOrderDateStartInput] = useState('');
  const [orderDateEndInput, setOrderDateEndInput] = useState('');
  const [orderDateStart, setOrderDateStart] = useState('');
  const [orderDateEnd, setOrderDateEnd] = useState('');
  const [dateValidationMessage, setDateValidationMessage] = useState('');
  const [currentPage, setCurrentPage] = useState(INITIAL_PAGE);
  const [pageSize, setPageSize] = useState(DEFAULT_PAGE_SIZE);
  const [loading, setLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState('');
  const [pageResult, setPageResult] = useState<PageResult<SalesOrderRecord> | null>(null);
  const [detailOpen, setDetailOpen] = useState(false);
  const [detailLoading, setDetailLoading] = useState(false);
  const [detail, setDetail] = useState<SalesOrderRecord | null>(null);
  const [permissions, setPermissions] = useState<string[]>(() => getAuthSession()?.permissions ?? []);
  const actions = getSalesOrderActions(permissions);

  const loadOrders = useCallback(async () => {
    setLoading(true);
    setErrorMessage('');
    try {
      setPageResult(
        await pageSalesOrders({
          current: currentPage,
          size: pageSize,
          ...(orderNo ? { orderNo } : {}),
          ...(status ? { status } : {}),
          ...(orderDateStart ? { orderDateStart } : {}),
          ...(orderDateEnd ? { orderDateEnd } : {}),
        }),
      );
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error));
    } finally {
      setLoading(false);
    }
  }, [currentPage, pageSize, orderNo, status, orderDateStart, orderDateEnd]);

  useEffect(() => {
    void loadOrders();
  }, [loadOrders]);

  useEffect(() => {
    void refreshPermissions();
  }, []);

  const handleSearch = () => {
    const nextOrderDateStart = orderDateStartInput.trim();
    const nextOrderDateEnd = orderDateEndInput.trim();

    if (!isValidDateFilter(nextOrderDateStart) || !isValidDateFilter(nextOrderDateEnd)) {
      setDateValidationMessage(DATE_FORMAT_ERROR_MESSAGE);
      return;
    }

    setDateValidationMessage('');
    setOrderNo(orderNoInput.trim());
    setOrderDateStart(nextOrderDateStart);
    setOrderDateEnd(nextOrderDateEnd);
    setCurrentPage(INITIAL_PAGE);
  };

  const handleTableChange = (pagination: TablePaginationConfig) => {
    setCurrentPage(pagination.current ?? INITIAL_PAGE);
    setPageSize(pagination.pageSize ?? DEFAULT_PAGE_SIZE);
  };

  async function openDetail(order: SalesOrderRecord) {
    setDetailOpen(true);
    setDetailLoading(true);
    try {
      setDetail(await getSalesOrderDetail(order.id));
    } catch (error) {
      message.error(getApiErrorMessage(error));
    } finally {
      setDetailLoading(false);
    }
  }

  async function runAction(action: () => Promise<void>, successMessage: string) {
    try {
      await action();
      message.success(successMessage);
      await loadOrders();
    } catch (error) {
      message.error(getApiErrorMessage(error));
    }
  }

  async function refreshPermissions() {
    try {
      const response = await getCurrentUserPermissions();
      setPermissions(response.permissions);
      const session = getAuthSession();
      if (session) {
        setAuthSession({ ...session, permissions: response.permissions, menuTree: response.menuTree });
      }
    } catch {
      setPermissions(getAuthSession()?.permissions ?? []);
    }
  }

  if (loading && !pageResult) {
    return <LoadingState message="正在加载销售订单" />;
  }

  if (errorMessage && !pageResult) {
    return <ErrorState message={errorMessage} onRetry={loadOrders} />;
  }

  return (
    <div className="system-page sales-order-list-page">
      <section className="system-page-topbar">
        <div>
          <h1>销售订单</h1>
          <p>按订单编号、状态和订单日期跟踪销售订单履约进度。</p>
        </div>
      </section>

      <ProCard className="system-page-card" bordered={false}>
        <Space className="system-page-toolbar" wrap>
          <Input
            allowClear
            placeholder="搜索订单编号"
            prefix={<SearchOutlined />}
            value={orderNoInput}
            onChange={(event) => setOrderNoInput(event.target.value)}
            onPressEnter={handleSearch}
          />
          <Select aria-label="订单状态筛选" options={statusOptions} value={status} onChange={(value) => { setStatus(value); setCurrentPage(INITIAL_PAGE); }} />
          <Input
            placeholder="订单开始日期"
            value={orderDateStartInput}
            onChange={(event) => setOrderDateStartInput(event.target.value)}
          />
          <Input
            placeholder="订单结束日期"
            value={orderDateEndInput}
            onChange={(event) => setOrderDateEndInput(event.target.value)}
          />
          <Button icon={<SearchOutlined />} onClick={handleSearch}>
            搜索
          </Button>
          <Button icon={<ReloadOutlined />} onClick={loadOrders}>
            刷新
          </Button>
        </Space>
        {dateValidationMessage ? <p className="ant-form-item-explain-error">{dateValidationMessage}</p> : null}

        {pageResult?.records.length === 0 ? (
          <EmptyState message="暂无销售订单" />
        ) : (
          <Table<SalesOrderRecord>
            rowKey="id"
            columns={buildColumns(actions, {
              onCancel: (order) => runAction(() => cancelSalesOrder(order.id), '订单已取消'),
              onDelete: (order) => runAction(() => deleteSalesOrder(order.id), '订单已删除'),
              onDetail: openDetail,
              onResubmit: (order) => runAction(() => resubmitSalesOrder(order.id), '订单已重新提交'),
              onSubmit: (order) => runAction(() => submitSalesOrder(order.id), '订单已提交审批'),
            })}
            dataSource={pageResult?.records ?? []}
            loading={loading}
            pagination={{
              current: Number(pageResult?.current ?? currentPage),
              pageSize: Number(pageResult?.size ?? pageSize),
              total: Number(pageResult?.total ?? 0),
              showSizeChanger: true,
            }}
            onChange={handleTableChange}
          />
        )}
      </ProCard>

      <Modal title={detail?.orderNo ?? '销售订单详情'} open={detailOpen} footer={null} width={920} onCancel={() => setDetailOpen(false)}>
        <ProCard loading={detailLoading} bordered={false}>
          {detail ? (
            <Space direction="vertical" style={{ width: '100%' }}>
              <Descriptions column={3} size="small">
                <Descriptions.Item label="客户">{detail.customerName || '-'}</Descriptions.Item>
                <Descriptions.Item label="季节">{detail.seasonName || '-'}</Descriptions.Item>
                <Descriptions.Item label="状态">{getStatusLabel(detail)}</Descriptions.Item>
                <Descriptions.Item label="总数量">{detail.totalQuantity ?? 0}</Descriptions.Item>
                <Descriptions.Item label="实际金额">{formatMoney(detail.actualAmount)}</Descriptions.Item>
                <Descriptions.Item label="交货日期">{detail.deliveryDate || '-'}</Descriptions.Item>
              </Descriptions>
              <Table<SalesOrderLineRecord>
                rowKey="id"
                size="small"
                pagination={false}
                columns={lineColumns}
                dataSource={detail.lines ?? []}
              />
            </Space>
          ) : null}
        </ProCard>
      </Modal>
    </div>
  );
}

function buildColumns(
  actions: SalesOrderActions,
  handlers: {
    onCancel: (order: SalesOrderRecord) => void;
    onDelete: (order: SalesOrderRecord) => void;
    onDetail: (order: SalesOrderRecord) => void;
    onResubmit: (order: SalesOrderRecord) => void;
    onSubmit: (order: SalesOrderRecord) => void;
  },
): ColumnsType<SalesOrderRecord> {
  return [
    { title: '订单编号', dataIndex: 'orderNo', key: 'orderNo', width: 170 },
    { title: '客户', dataIndex: 'customerName', key: 'customerName', render: (value) => value || '-' },
    { title: '季节', dataIndex: 'seasonName', key: 'seasonName', render: (value) => value || '-' },
    { title: '订单日期', dataIndex: 'orderDate', key: 'orderDate', width: 120 },
    { title: '交货日期', dataIndex: 'deliveryDate', key: 'deliveryDate', width: 120, render: (value) => value || '-' },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 120,
      render: (_, record) => <Tag color={statusColorMap[record.status] ?? 'default'}>{getStatusLabel(record)}</Tag>,
    },
    { title: '总数量', dataIndex: 'totalQuantity', key: 'totalQuantity', width: 100, render: (value) => value ?? 0 },
    { title: '实际金额', dataIndex: 'actualAmount', key: 'actualAmount', width: 120, render: formatMoney },
    {
      title: '操作',
      key: 'actions',
      width: 280,
      render: (_, record) => (
        <Space wrap>
          <Button icon={<EyeOutlined />} size="small" aria-label={`详情 ${record.orderNo}`} onClick={() => handlers.onDetail(record)}>
            详情
          </Button>
          {actions.canSubmit && record.status === 'DRAFT' ? (
            <Button size="small" aria-label={`提交 ${record.orderNo}`} onClick={() => handlers.onSubmit(record)}>
              提交
            </Button>
          ) : null}
          {actions.canResubmit && record.status === 'REJECTED' ? (
            <Button size="small" aria-label={`重新提交 ${record.orderNo}`} onClick={() => handlers.onResubmit(record)}>
              重新提交
            </Button>
          ) : null}
          {actions.canCancel && ['DRAFT', 'CONFIRMED'].includes(record.status) ? (
            <Popconfirm title="确认取消该销售订单？" okText="确认取消" cancelText="取消" onConfirm={() => handlers.onCancel(record)}>
              <Button size="small" aria-label={`取消 ${record.orderNo}`}>
                取消
              </Button>
            </Popconfirm>
          ) : null}
          {actions.canDelete && record.status === 'DRAFT' ? (
            <Popconfirm title="确认删除该草稿订单？" okText="确认删除" cancelText="取消" onConfirm={() => handlers.onDelete(record)}>
              <Button danger icon={<DeleteOutlined />} size="small" aria-label={`删除 ${record.orderNo}`}>
                删除
              </Button>
            </Popconfirm>
          ) : null}
        </Space>
      ),
    },
  ];
}

const lineColumns: ColumnsType<SalesOrderLineRecord> = [
  { title: '行号', dataIndex: 'lineNo', key: 'lineNo', width: 80 },
  { title: '款式', dataIndex: 'spuName', key: 'spuName', render: (_, record) => record.spuName || record.spuCode || '-' },
  { title: '颜色', dataIndex: 'colorName', key: 'colorName', render: (_, record) => record.colorName || record.colorCode || '-' },
  { title: '尺码矩阵', dataIndex: 'sizeMatrix', key: 'sizeMatrix', render: renderSizeMatrix },
  { title: '数量', dataIndex: 'totalQuantity', key: 'totalQuantity', width: 90, render: (value) => value ?? 0 },
  { title: '单价', dataIndex: 'unitPrice', key: 'unitPrice', width: 90, render: formatMoney },
  { title: '实际金额', dataIndex: 'actualAmount', key: 'actualAmount', width: 110, render: formatMoney },
];

function renderSizeMatrix(value?: Record<string, unknown> | null) {
  const sizes = Array.isArray(value?.sizes) ? value.sizes : [];
  if (sizes.length === 0) {
    return '-';
  }

  return (
    <Space wrap>
      {sizes.map((item) => {
        const size = item as { code?: string; quantity?: number };
        return <Tag key={size.code}>{`${size.code}: ${size.quantity ?? 0}`}</Tag>;
      })}
    </Space>
  );
}

function getSalesOrderActions(permissions: string[]): SalesOrderActions {
  return {
    canCancel: permissions.includes('order:sales:cancel'),
    canDelete: permissions.includes('order:sales:delete'),
    canResubmit: permissions.includes('order:sales:resubmit'),
    canSubmit: permissions.includes('order:sales:submit'),
  };
}

function getStatusLabel(order: SalesOrderRecord): string {
  return order.statusLabel || statusOptions.find((option) => option.value === order.status)?.label || order.status;
}

function formatMoney(value?: number | null): string {
  return Number(value ?? 0).toFixed(2);
}

function isValidDateFilter(value: string): boolean {
  if (!value) {
    return true;
  }

  return DATE_FORMAT_PATTERN.test(value);
}
