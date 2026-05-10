import { EyeOutlined, ReloadOutlined, SearchOutlined } from '@ant-design/icons';
import { ProCard } from '@ant-design/pro-components';
import { App, Button, Descriptions, Input, Modal, Select, Space, Table, Tag } from 'antd';
import type { ColumnsType, TablePaginationConfig } from 'antd/es/table';
import { useCallback, useEffect, useState } from 'react';
import { EmptyState, ErrorState, LoadingState } from '@/components/state';
import { getCurrentUserPermissions } from '@/services/auth/authService';
import { getApiErrorMessage } from '@/services/http/apiClient';
import type { PageResult } from '@/services/master/customerService';
import {
  fireProductionLineEvent,
  fireProductionOrderEvent,
  getProductionLineAvailableActions,
  getProductionOrderAvailableActions,
  getProductionOrderDetail,
  pageProductionOrders,
  type ProductionActionRecord,
  type ProductionOrderLineRecord,
  type ProductionOrderRecord,
} from '@/services/order/productionOrderService';
import { getAuthSession, setAuthSession } from '@/shared/storage/authSessionStorage';

const DEFAULT_PAGE_SIZE = 10;
const INITIAL_PAGE = 1;
const DATE_FORMAT_PATTERN = /^\d{4}-\d{2}-\d{2}$/;
const DATE_FORMAT_ERROR_MESSAGE = '计划日期格式必须为 YYYY-MM-DD';

const statusOptions = [
  { label: '全部状态', value: '' },
  { label: '草稿', value: 'DRAFT' },
  { label: '已下达', value: 'RELEASED' },
  { label: '已排产', value: 'PLANNED' },
  { label: '裁剪中', value: 'CUTTING' },
  { label: '缝制中', value: 'SEWING' },
  { label: '后整中', value: 'FINISHING' },
  { label: '已完工', value: 'COMPLETED' },
  { label: '已入库', value: 'STOCKED' },
];

const statusColorMap: Record<string, string> = {
  COMPLETED: 'green',
  CUTTING: 'orange',
  DRAFT: 'default',
  FINISHING: 'cyan',
  PLANNED: 'blue',
  RELEASED: 'geekblue',
  SEWING: 'purple',
  STOCKED: 'green',
};

interface ProductionOrderActions {
  canFireLineEvent: boolean;
  canFireOrderEvent: boolean;
}

export function ProductionOrderListPage() {
  const { message } = App.useApp();
  const [orderNoInput, setOrderNoInput] = useState('');
  const [orderNo, setOrderNo] = useState('');
  const [status, setStatus] = useState('');
  const [planDateStartInput, setPlanDateStartInput] = useState('');
  const [planDateEndInput, setPlanDateEndInput] = useState('');
  const [planDateStart, setPlanDateStart] = useState('');
  const [planDateEnd, setPlanDateEnd] = useState('');
  const [dateValidationMessage, setDateValidationMessage] = useState('');
  const [currentPage, setCurrentPage] = useState(INITIAL_PAGE);
  const [pageSize, setPageSize] = useState(DEFAULT_PAGE_SIZE);
  const [loading, setLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState('');
  const [pageResult, setPageResult] = useState<PageResult<ProductionOrderRecord> | null>(null);
  const [detailOpen, setDetailOpen] = useState(false);
  const [detailLoading, setDetailLoading] = useState(false);
  const [detail, setDetail] = useState<ProductionOrderRecord | null>(null);
  const [orderActions, setOrderActions] = useState<ProductionActionRecord[]>([]);
  const [lineActions, setLineActions] = useState<Record<string, ProductionActionRecord[]>>({});
  const [permissions, setPermissions] = useState<string[]>(() => getAuthSession()?.permissions ?? []);
  const actions = getProductionOrderActions(permissions);

  const loadOrders = useCallback(async () => {
    setLoading(true);
    setErrorMessage('');
    try {
      setPageResult(
        await pageProductionOrders({
          current: currentPage,
          size: pageSize,
          ...(orderNo ? { orderNo } : {}),
          ...(status ? { status } : {}),
          ...(planDateStart ? { planDateStart } : {}),
          ...(planDateEnd ? { planDateEnd } : {}),
        }),
      );
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error));
    } finally {
      setLoading(false);
    }
  }, [currentPage, pageSize, orderNo, status, planDateStart, planDateEnd]);

  useEffect(() => {
    void loadOrders();
  }, [loadOrders]);

  useEffect(() => {
    void refreshPermissions();
  }, []);

  const handleSearch = () => {
    const nextPlanDateStart = planDateStartInput.trim();
    const nextPlanDateEnd = planDateEndInput.trim();

    if (!isValidDateFilter(nextPlanDateStart) || !isValidDateFilter(nextPlanDateEnd)) {
      setDateValidationMessage(DATE_FORMAT_ERROR_MESSAGE);
      return;
    }

    setDateValidationMessage('');
    setOrderNo(orderNoInput.trim());
    setPlanDateStart(nextPlanDateStart);
    setPlanDateEnd(nextPlanDateEnd);
    setCurrentPage(INITIAL_PAGE);
  };

  const handleTableChange = (pagination: TablePaginationConfig) => {
    setCurrentPage(pagination.current ?? INITIAL_PAGE);
    setPageSize(pagination.pageSize ?? DEFAULT_PAGE_SIZE);
  };

  async function openDetail(order: ProductionOrderRecord) {
    setDetailOpen(true);
    setDetailLoading(true);
    try {
      const [orderDetail, availableActions] = await Promise.all([
        getProductionOrderDetail(order.id),
        getProductionOrderAvailableActions(order.id),
      ]);
      const nextLineActions = await loadLineActions(orderDetail);
      setDetail(orderDetail);
      setOrderActions(availableActions);
      setLineActions(nextLineActions);
    } catch (error) {
      message.error(getApiErrorMessage(error));
      setDetailOpen(false);
    } finally {
      setDetailLoading(false);
    }
  }

  async function loadLineActions(order: ProductionOrderRecord) {
    const entries = await Promise.all(
      (order.lines ?? []).map(async (line) => [
        line.id,
        await getProductionLineAvailableActions(order.id, line.id),
      ] as const),
    );
    return Object.fromEntries(entries);
  }

  async function handleFireOrderEvent(action: ProductionActionRecord) {
    if (!detail) {
      return;
    }

    try {
      await fireProductionOrderEvent({ orderId: detail.id, event: action.event });
      message.success(`${action.label}成功`);
      await reloadDetail(detail.id);
      await loadOrders();
    } catch (error) {
      message.error(getApiErrorMessage(error));
    }
  }

  async function handleFireLineEvent(line: ProductionOrderLineRecord, action: ProductionActionRecord) {
    if (!detail) {
      return;
    }

    try {
      await fireProductionLineEvent({ orderId: detail.id, lineId: line.id, event: action.event });
      message.success(`${action.label}成功`);
      await reloadDetail(detail.id);
      await loadOrders();
    } catch (error) {
      message.error(getApiErrorMessage(error));
    }
  }

  async function reloadDetail(orderId: string) {
    const [orderDetail, availableActions] = await Promise.all([
      getProductionOrderDetail(orderId),
      getProductionOrderAvailableActions(orderId),
    ]);
    setDetail(orderDetail);
    setOrderActions(availableActions);
    setLineActions(await loadLineActions(orderDetail));
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
    return <LoadingState message="正在加载生产订单" />;
  }

  if (errorMessage && !pageResult) {
    return <ErrorState message={errorMessage} onRetry={loadOrders} />;
  }

  return (
    <div className="system-page production-order-list-page">
      <section className="system-page-topbar">
        <div>
          <h1>生产订单</h1>
          <p>跟踪生产单计划、工序状态、完工与入库进度。</p>
        </div>
      </section>

      <ProCard className="system-page-card" bordered={false}>
        <Space className="system-page-toolbar" wrap>
          <Input
            allowClear
            placeholder="搜索生产单号"
            prefix={<SearchOutlined />}
            value={orderNoInput}
            onChange={(event) => setOrderNoInput(event.target.value)}
            onPressEnter={handleSearch}
          />
          <Select aria-label="生产状态筛选" options={statusOptions} value={status} onChange={(value) => { setStatus(value); setCurrentPage(INITIAL_PAGE); }} />
          <Input
            placeholder="计划开始日期"
            value={planDateStartInput}
            onChange={(event) => setPlanDateStartInput(event.target.value)}
          />
          <Input
            placeholder="计划结束日期"
            value={planDateEndInput}
            onChange={(event) => setPlanDateEndInput(event.target.value)}
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
          <EmptyState message="暂无生产订单" />
        ) : (
          <Table<ProductionOrderRecord>
            rowKey="id"
            columns={buildColumns(openDetail)}
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

      <ProductionDetailModal
        actions={actions}
        detail={detail}
        lineActions={lineActions}
        loading={detailLoading}
        open={detailOpen}
        orderActions={orderActions}
        onCancel={() => setDetailOpen(false)}
        onFireLineEvent={handleFireLineEvent}
        onFireOrderEvent={handleFireOrderEvent}
      />
    </div>
  );
}

function buildColumns(onDetail: (order: ProductionOrderRecord) => void): ColumnsType<ProductionOrderRecord> {
  return [
    { title: '生产单号', dataIndex: 'orderNo', key: 'orderNo', width: 180 },
    { title: '来源', dataIndex: 'sourceType', key: 'sourceType', width: 120, render: formatSourceType },
    { title: '计划日期', dataIndex: 'planDate', key: 'planDate', width: 120, render: (value) => value || '-' },
    { title: '完工期限', dataIndex: 'deadlineDate', key: 'deadlineDate', width: 120, render: (value) => value || '-' },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 120,
      render: (_, record) => <Tag color={statusColorMap[record.status ?? ''] ?? 'default'}>{getStatusLabel(record)}</Tag>,
    },
    { title: '总数量', dataIndex: 'totalQuantity', key: 'totalQuantity', width: 100, render: (value) => value ?? 0 },
    { title: '已完工', dataIndex: 'completedQuantity', key: 'completedQuantity', width: 100, render: (value) => value ?? 0 },
    { title: '已入库', dataIndex: 'stockedQuantity', key: 'stockedQuantity', width: 100, render: (value) => value ?? 0 },
    {
      title: '操作',
      key: 'actions',
      width: 120,
      render: (_, record) => (
        <Button icon={<EyeOutlined />} size="small" aria-label={`详情 ${record.orderNo}`} onClick={() => onDetail(record)}>
          详情
        </Button>
      ),
    },
  ];
}

function ProductionDetailModal({
  actions,
  detail,
  lineActions,
  loading,
  open,
  orderActions,
  onCancel,
  onFireLineEvent,
  onFireOrderEvent,
}: {
  actions: ProductionOrderActions;
  detail: ProductionOrderRecord | null;
  lineActions: Record<string, ProductionActionRecord[]>;
  loading: boolean;
  open: boolean;
  orderActions: ProductionActionRecord[];
  onCancel: () => void;
  onFireLineEvent: (line: ProductionOrderLineRecord, action: ProductionActionRecord) => void;
  onFireOrderEvent: (action: ProductionActionRecord) => void;
}) {
  return (
    <Modal
      footer={null}
      getContainer={false}
      onCancel={onCancel}
      open={open}
      title={detail?.orderNo ?? '生产订单详情'}
      width={980}
    >
      <ProCard loading={loading} bordered={false}>
        {detail ? (
          <Space direction="vertical" style={{ width: '100%' }}>
            <Descriptions column={3} size="small">
              <Descriptions.Item label="生产单号">{detail.orderNo}</Descriptions.Item>
              <Descriptions.Item label="状态">{getStatusLabel(detail)}</Descriptions.Item>
              <Descriptions.Item label="来源">{formatSourceType(detail.sourceType)}</Descriptions.Item>
              <Descriptions.Item label="计划日期">{detail.planDate || '-'}</Descriptions.Item>
              <Descriptions.Item label="完工期限">{detail.deadlineDate || '-'}</Descriptions.Item>
              <Descriptions.Item label="总数量">{detail.totalQuantity ?? 0}</Descriptions.Item>
              <Descriptions.Item label="已完工">{detail.completedQuantity ?? 0}</Descriptions.Item>
              <Descriptions.Item label="已入库">{detail.stockedQuantity ?? 0}</Descriptions.Item>
              <Descriptions.Item label="备注">{detail.remark || '-'}</Descriptions.Item>
            </Descriptions>
            {actions.canFireOrderEvent && orderActions.length > 0 ? (
              <Space wrap>
                {orderActions.map((action) => (
                  <Button key={action.event} type="primary" onClick={() => onFireOrderEvent(action)}>
                    {action.label}
                  </Button>
                ))}
              </Space>
            ) : null}
            <Table<ProductionOrderLineRecord>
              rowKey="id"
              size="small"
              pagination={false}
              columns={buildLineColumns(actions, lineActions, onFireLineEvent)}
              dataSource={detail.lines ?? []}
            />
          </Space>
        ) : null}
      </ProCard>
    </Modal>
  );
}

function buildLineColumns(
  actions: ProductionOrderActions,
  lineActions: Record<string, ProductionActionRecord[]>,
  onFireLineEvent: (line: ProductionOrderLineRecord, action: ProductionActionRecord) => void,
): ColumnsType<ProductionOrderLineRecord> {
  return [
    { title: '行号', dataIndex: 'lineNo', key: 'lineNo', width: 70 },
    { title: '款式', dataIndex: 'spuName', key: 'spuName', render: (_, record) => record.spuName || record.spuCode || '-' },
    { title: '颜色', dataIndex: 'colorName', key: 'colorName', render: (_, record) => record.colorName || record.colorCode || '-' },
    { title: '尺码矩阵', dataIndex: 'sizeMatrix', key: 'sizeMatrix', render: renderSizeMatrix },
    { title: '数量', dataIndex: 'totalQuantity', key: 'totalQuantity', width: 80, render: (value) => value ?? 0 },
    { title: '已完工', dataIndex: 'completedQuantity', key: 'completedQuantity', width: 90, render: (value) => value ?? 0 },
    { title: '已入库', dataIndex: 'stockedQuantity', key: 'stockedQuantity', width: 90, render: (value) => value ?? 0 },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 110,
      render: (_, record) => <Tag color={statusColorMap[record.status ?? ''] ?? 'default'}>{record.statusLabel || record.status || '-'}</Tag>,
    },
    {
      title: '行操作',
      key: 'lineActions',
      width: 180,
      render: (_, record) => (
        <Space wrap>
          {actions.canFireLineEvent
            ? (lineActions[record.id] ?? []).map((action) => (
              <Button key={action.event} size="small" aria-label={`${action.label} ${record.id}`} onClick={() => onFireLineEvent(record, action)}>
                {action.label}
              </Button>
            ))
            : null}
        </Space>
      ),
    },
  ];
}

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

function getProductionOrderActions(permissions: string[]): ProductionOrderActions {
  return {
    canFireLineEvent: permissions.includes('order:production:fire-line-event'),
    canFireOrderEvent: permissions.includes('order:production:fire-event'),
  };
}

function getStatusLabel(order: ProductionOrderRecord): string {
  return order.statusLabel || statusOptions.find((option) => option.value === order.status)?.label || order.status || '-';
}

function formatSourceType(value?: string | null): string {
  if (value === 'SALES_ORDER') {
    return '销售订单';
  }
  if (value === 'MANUAL') {
    return '手工创建';
  }
  return value || '-';
}

function isValidDateFilter(value: string): boolean {
  if (!value) {
    return true;
  }

  return DATE_FORMAT_PATTERN.test(value);
}
