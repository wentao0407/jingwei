import { DeleteOutlined, EditOutlined, EyeOutlined, PlusOutlined, ReloadOutlined, SearchOutlined } from '@ant-design/icons';
import { ProCard } from '@ant-design/pro-components';
import { App, Button, Checkbox, Descriptions, Form, Input, InputNumber, Modal, Popconfirm, Select, Space, Table, Tag } from 'antd';
import type { ColumnsType, TablePaginationConfig } from 'antd/es/table';
import { useCallback, useEffect, useState } from 'react';
import { EmptyState, ErrorState, LoadingState } from '@/components/state';
import { getCurrentUserPermissions } from '@/services/auth/authService';
import { getApiErrorMessage } from '@/services/http/apiClient';
import { listCustomers, type CustomerRecord, type PageResult } from '@/services/master/customerService';
import { listSeasons, type SeasonRecord } from '@/services/master/seasonService';
import { listSizeGroups, type SizeGroupRecord } from '@/services/master/sizeGroupService';
import { listSpus, type ColorWayRecord, type SpuRecord } from '@/services/master/spuService';
import {
  cancelSalesOrder,
  convertSalesOrderToProduction,
  createSalesOrder,
  createQuantityChange,
  deleteSalesOrder,
  getSalesOrderDetail,
  pageSalesOrders,
  resubmitSalesOrder,
  submitSalesOrder,
  updateSalesOrder,
  type SalesOrderLineRecord,
  type SalesOrderRecord,
  type ConvertSalesOrderPayload,
  type QuantityChangePayload,
  type SaveSalesOrderPayload,
} from '@/services/order/salesOrderService';
import {
  createReturnOrder,
  type CreateReturnOrderPayload,
} from '@/services/order/returnOrderService';
import { getAuthSession, setAuthSession } from '@/shared/storage/authSessionStorage';

const DEFAULT_PAGE_SIZE = 10;
const INITIAL_PAGE = 1;
const DATE_FORMAT_PATTERN = /^\d{4}-\d{2}-\d{2}$/;
const DATE_FORMAT_ERROR_MESSAGE = '订单日期格式必须为 YYYY-MM-DD';
const DEFAULT_DISCOUNT_RATE = 1;
const DEFAULT_UNIT_PRICE = 0;
const MAX_REMARK_LENGTH = 500;

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
  canConvert: boolean;
  canCreate: boolean;
  canDelete: boolean;
  canQuantityChange: boolean;
  canReturn: boolean;
  canUpdate: boolean;
  canResubmit: boolean;
  canSubmit: boolean;
}

interface SalesOrderFormLine {
  spuId?: string;
  colorWayId?: string;
  sizeGroupId?: string;
  sizes?: Record<string, number>;
  unitPrice?: number;
  discountRate?: number;
  deliveryDate?: string;
  remark?: string;
}

interface SalesOrderFormValues {
  customerId?: string;
  seasonId?: string;
  orderDate?: string;
  deliveryDate?: string;
  remark?: string;
  lines?: SalesOrderFormLine[];
}

type FormMode = 'create' | 'edit';

interface ConvertFormValues {
  lineIds?: string[];
  skipCuttingLineIds?: string[];
  deadlineDate?: string;
  remark?: string;
}

interface QuantityChangeFormValues {
  orderLineId?: string;
  sizes?: Record<string, number>;
  reason?: string;
}

interface ReturnOrderFormValues {
  lineIds?: string[];
  returnType?: string;
  sizes?: Record<string, Record<string, number>>;
  reason?: string;
  remark?: string;
}

export function SalesOrderListPage() {
  const { message } = App.useApp();
  const [form] = Form.useForm<SalesOrderFormValues>();
  const [convertForm] = Form.useForm<ConvertFormValues>();
  const [quantityChangeForm] = Form.useForm<QuantityChangeFormValues>();
  const [returnOrderForm] = Form.useForm<ReturnOrderFormValues>();
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
  const [formMode, setFormMode] = useState<FormMode>('create');
  const [formOpen, setFormOpen] = useState(false);
  const [formSaving, setFormSaving] = useState(false);
  const [formLoading, setFormLoading] = useState(false);
  const [editingOrder, setEditingOrder] = useState<SalesOrderRecord | null>(null);
  const [customers, setCustomers] = useState<CustomerRecord[]>([]);
  const [seasons, setSeasons] = useState<SeasonRecord[]>([]);
  const [spus, setSpus] = useState<SpuRecord[]>([]);
  const [sizeGroups, setSizeGroups] = useState<SizeGroupRecord[]>([]);
  const [convertOpen, setConvertOpen] = useState(false);
  const [convertLoading, setConvertLoading] = useState(false);
  const [convertSaving, setConvertSaving] = useState(false);
  const [convertOrder, setConvertOrder] = useState<SalesOrderRecord | null>(null);
  const [quantityChangeOpen, setQuantityChangeOpen] = useState(false);
  const [quantityChangeLoading, setQuantityChangeLoading] = useState(false);
  const [quantityChangeSaving, setQuantityChangeSaving] = useState(false);
  const [quantityChangeOrder, setQuantityChangeOrder] = useState<SalesOrderRecord | null>(null);
  const [returnOrderOpen, setReturnOrderOpen] = useState(false);
  const [returnOrderLoading, setReturnOrderLoading] = useState(false);
  const [returnOrderSaving, setReturnOrderSaving] = useState(false);
  const [returnOrder, setReturnOrder] = useState<SalesOrderRecord | null>(null);
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

  async function openCreateForm() {
    setFormMode('create');
    setEditingOrder(null);
    setFormOpen(true);
    await ensureFormOptionsLoaded();
    form.setFieldsValue({
      orderDate: '',
      lines: [createEmptyFormLine()],
    });
  }

  async function openEditForm(order: SalesOrderRecord) {
    setFormMode('edit');
    setFormOpen(true);
    setFormLoading(true);
    try {
      await ensureFormOptionsLoaded();
      const orderDetail = await getSalesOrderDetail(order.id);
      setEditingOrder(orderDetail);
      form.setFieldsValue(toFormValues(orderDetail));
    } catch (error) {
      message.error(getApiErrorMessage(error));
      setFormOpen(false);
    } finally {
      setFormLoading(false);
    }
  }

  async function handleSaveOrder() {
    try {
      const values = await form.validateFields();
      const validationMessage = validateOrderFormValues(values);
      if (validationMessage) {
        message.error(validationMessage);
        return;
      }

      setFormSaving(true);
      if (formMode === 'create') {
        await createSalesOrder(toSavePayload(values, true, sizeGroups));
        message.success('订单创建成功');
      } else if (editingOrder) {
        await updateSalesOrder(editingOrder.id, toSavePayload(values, false, sizeGroups));
        message.success('订单更新成功');
      }
      setFormOpen(false);
      await loadOrders();
    } catch (error) {
      if (isFormValidationError(error)) {
        return;
      }
      message.error(getApiErrorMessage(error));
    } finally {
      setFormSaving(false);
    }
  }

  async function openConvertForm(order: SalesOrderRecord) {
    setConvertOpen(true);
    setConvertLoading(true);
    try {
      const orderDetail = await getSalesOrderDetail(order.id);
      setConvertOrder(orderDetail);
      convertForm.setFieldsValue({
        lineIds: [],
        skipCuttingLineIds: [],
      });
    } catch (error) {
      message.error(getApiErrorMessage(error));
      setConvertOpen(false);
    } finally {
      setConvertLoading(false);
    }
  }

  async function handleConvertToProduction() {
    try {
      const values = await convertForm.validateFields();
      const validationMessage = validateConvertFormValues(values);
      if (validationMessage || !convertOrder) {
        message.error(validationMessage ?? '销售订单数据不存在');
        return;
      }

      setConvertSaving(true);
      await convertSalesOrderToProduction(toConvertPayload(convertOrder, values));
      message.success('生产订单已生成');
      setConvertOpen(false);
      await loadOrders();
    } catch (error) {
      if (isFormValidationError(error)) {
        return;
      }
      message.error(getApiErrorMessage(error));
    } finally {
      setConvertSaving(false);
    }
  }

  async function openQuantityChangeForm(order: SalesOrderRecord) {
    setQuantityChangeOpen(true);
    setQuantityChangeLoading(true);
    try {
      const orderDetail = await getSalesOrderDetail(order.id);
      const firstLine = orderDetail.lines?.[0];
      setQuantityChangeOrder(orderDetail);
      quantityChangeForm.setFieldsValue({
        orderLineId: firstLine?.id,
        sizes: firstLine ? createSizeQuantityMapFromLine(firstLine) : {},
        reason: '',
      });
    } catch (error) {
      message.error(getApiErrorMessage(error));
      setQuantityChangeOpen(false);
    } finally {
      setQuantityChangeLoading(false);
    }
  }

  async function handleCreateQuantityChange() {
    try {
      const values = await quantityChangeForm.validateFields();
      if (!quantityChangeOrder) {
        message.error('销售订单数据不存在');
        return;
      }

      setQuantityChangeSaving(true);
      await createQuantityChange(toQuantityChangePayload(quantityChangeOrder, values));
      message.success('数量变更已提交');
      setQuantityChangeOpen(false);
      await loadOrders();
    } catch (error) {
      if (isFormValidationError(error)) {
        return;
      }
      message.error(getApiErrorMessage(error));
    } finally {
      setQuantityChangeSaving(false);
    }
  }

  async function openReturnOrderForm(order: SalesOrderRecord) {
    setReturnOrderOpen(true);
    setReturnOrderLoading(true);
    try {
      const orderDetail = await getSalesOrderDetail(order.id);
      setReturnOrder(orderDetail);
      returnOrderForm.setFieldsValue({
        lineIds: [],
        returnType: 'CUSTOMER_REJECT',
        sizes: {},
        reason: '',
        remark: '',
      });
    } catch (error) {
      message.error(getApiErrorMessage(error));
      setReturnOrderOpen(false);
    } finally {
      setReturnOrderLoading(false);
    }
  }

  async function handleCreateReturnOrder() {
    try {
      const values = await returnOrderForm.validateFields();
      const validationMessage = validateReturnOrderFormValues(returnOrder, values);
      if (validationMessage || !returnOrder) {
        message.error(validationMessage ?? '销售订单数据不存在');
        return;
      }

      setReturnOrderSaving(true);
      await createReturnOrder(toReturnOrderPayload(returnOrder, values));
      message.success('退货单已创建');
      setReturnOrderOpen(false);
      await loadOrders();
    } catch (error) {
      if (isFormValidationError(error)) {
        return;
      }
      message.error(getApiErrorMessage(error));
    } finally {
      setReturnOrderSaving(false);
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

  async function ensureFormOptionsLoaded() {
    if (customers.length > 0 && seasons.length > 0 && spus.length > 0 && sizeGroups.length > 0) {
      return;
    }

    const [customerPage, seasonList, spuList, sizeGroupList] = await Promise.all([
      listCustomers({ current: 1, size: 200, status: 'ACTIVE' }),
      listSeasons({ status: 'ACTIVE' }),
      listSpus({ status: 'ACTIVE' }),
      listSizeGroups({ status: 'ACTIVE' }),
    ]);
    setCustomers(customerPage.records);
    setSeasons(seasonList);
    setSpus(spuList);
    setSizeGroups(sizeGroupList);
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
        {actions.canCreate ? (
          <Button type="primary" icon={<PlusOutlined />} aria-label="新建订单" onClick={openCreateForm}>
            新建订单
          </Button>
        ) : null}
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
              onEdit: openEditForm,
              onConvert: openConvertForm,
              onQuantityChange: openQuantityChangeForm,
              onReturn: openReturnOrderForm,
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

      <Modal
        title={formMode === 'create' ? '新建销售订单' : '编辑销售订单'}
        aria-label={formMode === 'create' ? '新建销售订单' : '编辑销售订单'}
        open={formOpen}
        width={980}
        getContainer={false}
        confirmLoading={formSaving}
        okText="保存订单"
        cancelText="取消"
        okButtonProps={{ 'aria-label': '保存订单' }}
        onCancel={() => setFormOpen(false)}
        onOk={handleSaveOrder}
        destroyOnHidden
        forceRender
      >
        <ProCard loading={formLoading} bordered={false}>
          <SalesOrderForm
            customers={customers}
            form={form}
            formMode={formMode}
            seasons={seasons}
            sizeGroups={sizeGroups}
            spus={spus}
          />
        </ProCard>
      </Modal>

      <Modal
        title={detail?.orderNo ?? '销售订单详情'}
        open={detailOpen}
        footer={renderDetailFooter(detail, actions, () => setDetailOpen(false), openEditForm)}
        width={920}
        getContainer={false}
        onCancel={() => setDetailOpen(false)}
      >
        <ProCard loading={detailLoading} bordered={false}>
          {detail ? (
            <Space direction="vertical" style={{ width: '100%' }}>
              <Descriptions column={3} size="small">
                <Descriptions.Item label="客户">{detail.customerName || '-'}</Descriptions.Item>
                <Descriptions.Item label="季节">{detail.seasonName || '-'}</Descriptions.Item>
                <Descriptions.Item label="状态">{getStatusLabel(detail)}</Descriptions.Item>
                <Descriptions.Item label="总数量">{detail.totalQuantity ?? 0}</Descriptions.Item>
                <Descriptions.Item label="订单总金额">{formatMoney(detail.totalAmount)}</Descriptions.Item>
                <Descriptions.Item label="折扣金额">{formatMoney(detail.discountAmount)}</Descriptions.Item>
                <Descriptions.Item label="实际金额">{formatMoney(detail.actualAmount)}</Descriptions.Item>
                <Descriptions.Item label="已收金额">{formatMoney(detail.paymentAmount)}</Descriptions.Item>
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

      {convertOpen ? (
        <Modal
          title="生成生产订单"
          aria-label="生成生产订单"
          open={convertOpen}
          width={760}
          getContainer={false}
          confirmLoading={convertSaving}
          okText="确认生成"
          cancelText="取消"
          okButtonProps={{ 'aria-label': '确认生成生产订单' }}
          onCancel={() => setConvertOpen(false)}
          onOk={handleConvertToProduction}
        >
          <ProCard loading={convertLoading} bordered={false}>
            <ConvertToProductionForm form={convertForm} order={convertOrder} />
          </ProCard>
        </Modal>
      ) : null}

      {quantityChangeOpen ? (
        <Modal
          title="创建数量变更"
          aria-label="创建数量变更"
          open={quantityChangeOpen}
          width={760}
          getContainer={false}
          confirmLoading={quantityChangeSaving}
          okText="提交变更"
          cancelText="取消"
          okButtonProps={{ 'aria-label': '提交数量变更' }}
          onCancel={() => setQuantityChangeOpen(false)}
          onOk={handleCreateQuantityChange}
        >
          <ProCard loading={quantityChangeLoading} bordered={false}>
            <QuantityChangeForm form={quantityChangeForm} order={quantityChangeOrder} />
          </ProCard>
        </Modal>
      ) : null}

      {returnOrderOpen ? (
        <Modal
          title="创建退货单"
          aria-label="创建退货单"
          open={returnOrderOpen}
          width={820}
          getContainer={false}
          confirmLoading={returnOrderSaving}
          okText="提交退货"
          cancelText="取消"
          okButtonProps={{ 'aria-label': '提交退货单' }}
          onCancel={() => setReturnOrderOpen(false)}
          onOk={handleCreateReturnOrder}
        >
          <ProCard loading={returnOrderLoading} bordered={false}>
            <ReturnOrderForm form={returnOrderForm} order={returnOrder} />
          </ProCard>
        </Modal>
      ) : null}
    </div>
  );
}

function buildColumns(
  actions: SalesOrderActions,
  handlers: {
    onCancel: (order: SalesOrderRecord) => void;
    onConvert: (order: SalesOrderRecord) => void;
    onDelete: (order: SalesOrderRecord) => void;
    onDetail: (order: SalesOrderRecord) => void;
    onEdit: (order: SalesOrderRecord) => void;
    onQuantityChange: (order: SalesOrderRecord) => void;
    onReturn: (order: SalesOrderRecord) => void;
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
          {actions.canUpdate && record.status === 'DRAFT' ? (
            <Button icon={<EditOutlined />} size="small" aria-label={`编辑 ${record.orderNo}`} onClick={() => handlers.onEdit(record)}>
              编辑
            </Button>
          ) : null}
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
          {actions.canConvert && record.status === 'CONFIRMED' ? (
            <Button size="small" aria-label={`生成生产 ${record.orderNo}`} onClick={() => handlers.onConvert(record)}>
              生成生产
            </Button>
          ) : null}
          {actions.canQuantityChange && record.status === 'CONFIRMED' ? (
            <Button size="small" aria-label={`数量变更 ${record.orderNo}`} onClick={() => handlers.onQuantityChange(record)}>
              数量变更
            </Button>
          ) : null}
          {actions.canReturn && ['SHIPPED', 'COMPLETED'].includes(record.status) ? (
            <Button size="small" aria-label={`创建退货 ${record.orderNo}`} onClick={() => handlers.onReturn(record)}>
              创建退货
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

function renderDetailFooter(
  detail: SalesOrderRecord | null,
  actions: SalesOrderActions,
  onClose: () => void,
  onEdit: (order: SalesOrderRecord) => void,
) {
  if (!detail || !actions.canUpdate || detail.status !== 'DRAFT') {
    return null;
  }

  return (
    <Button
      aria-label={`详情编辑 ${detail.orderNo}`}
      icon={<EditOutlined />}
      type="primary"
      onClick={() => {
        onClose();
        onEdit(detail);
      }}
    >
      编辑订单
    </Button>
  );
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

function getSalesOrderActions(permissions: string[]): SalesOrderActions {
  return {
    canCancel: permissions.includes('order:sales:cancel'),
    canConvert: permissions.includes('order:sales:convert'),
    canCreate: permissions.includes('order:sales:create'),
    canDelete: permissions.includes('order:sales:delete'),
    canQuantityChange: permissions.includes('order:sales:quantity-change'),
    canReturn: permissions.includes('order:return:create'),
    canUpdate: permissions.includes('order:sales:update'),
    canResubmit: permissions.includes('order:sales:resubmit'),
    canSubmit: permissions.includes('order:sales:submit'),
  };
}

function ReturnOrderForm({
  form,
  order,
}: {
  form: ReturnType<typeof Form.useForm<ReturnOrderFormValues>>[0];
  order: SalesOrderRecord | null;
}) {
  const lines = order?.lines ?? [];

  return (
    <Form<ReturnOrderFormValues> form={form} layout="vertical" preserve={false}>
      <Form.Item label="退货类型" name="returnType" rules={[{ required: true, message: '请选择退货类型' }]}>
        <Select
          aria-label="退货类型"
          options={[
            { label: '客户退货', value: 'CUSTOMER_REJECT' },
            { label: '物流拒收', value: 'LOGISTICS_REJECT' },
            { label: '经销商退货', value: 'DISTRIBUTOR_RETURN' },
          ]}
        />
      </Form.Item>
      <Form.Item label="退货明细" name="lineIds" rules={[{ required: true, message: '请选择至少一行退货明细' }]}>
        <Checkbox.Group style={{ width: '100%' }}>
          <Space direction="vertical" style={{ width: '100%' }}>
            {lines.map((line) => (
              <Checkbox aria-label={`退货行 ${line.id}`} key={line.id} value={line.id}>
                {formatLineLabel(line)}
              </Checkbox>
            ))}
          </Space>
        </Checkbox.Group>
      </Form.Item>
      {lines.map((line) => (
        <Space key={line.id} wrap>
          {getSizeEntries(line).map((size) => (
            <Form.Item key={size.sizeId} label={`退货尺码 ${size.code}`} name={['sizes', line.id, size.sizeId]}>
              <InputNumber min={0} max={size.quantity} precision={0} />
            </Form.Item>
          ))}
        </Space>
      ))}
      <Form.Item label="退货原因" name="reason" normalize={normalizeTextInput} rules={[{ required: true, message: '请输入退货原因' }]}>
        <Input.TextArea aria-label="退货原因" maxLength={MAX_REMARK_LENGTH} rows={3} />
      </Form.Item>
      <Form.Item label="退货备注" name="remark" normalize={normalizeTextInput}>
        <Input.TextArea aria-label="退货备注" maxLength={MAX_REMARK_LENGTH} rows={2} />
      </Form.Item>
    </Form>
  );
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

function ConvertToProductionForm({
  form,
  order,
}: {
  form: ReturnType<typeof Form.useForm<ConvertFormValues>>[0];
  order: SalesOrderRecord | null;
}) {
  const lines = order?.lines ?? [];

  return (
    <Form<ConvertFormValues> form={form} layout="vertical" preserve={false}>
      <Form.Item label="转生产明细" name="lineIds" rules={[{ required: true, message: '请选择至少一行订单行' }]}>
        <Checkbox.Group style={{ width: '100%' }}>
          <Space direction="vertical" style={{ width: '100%' }}>
            {lines.map((line) => (
              <Checkbox aria-label={`转生产行 ${line.id}`} key={line.id} value={line.id}>
                {formatLineLabel(line)}
              </Checkbox>
            ))}
          </Space>
        </Checkbox.Group>
      </Form.Item>
      <Form.Item label="跳过裁剪行" name="skipCuttingLineIds">
        <Checkbox.Group style={{ width: '100%' }}>
          <Space direction="vertical" style={{ width: '100%' }}>
            {lines.map((line) => (
              <Checkbox aria-label={`跳过裁剪 ${line.id}`} key={line.id} value={line.id}>
                {formatLineLabel(line)}
              </Checkbox>
            ))}
          </Space>
        </Checkbox.Group>
      </Form.Item>
      <Form.Item label="要求完工日期" name="deadlineDate" normalize={normalizeTextInput}>
        <Input aria-label="要求完工日期" placeholder="YYYY-MM-DD" />
      </Form.Item>
      <Form.Item label="转生产备注" name="remark" normalize={normalizeTextInput}>
        <Input.TextArea aria-label="转生产备注" maxLength={MAX_REMARK_LENGTH} rows={3} />
      </Form.Item>
    </Form>
  );
}

function QuantityChangeForm({
  form,
  order,
}: {
  form: ReturnType<typeof Form.useForm<QuantityChangeFormValues>>[0];
  order: SalesOrderRecord | null;
}) {
  const selectedLineId = Form.useWatch('orderLineId', form);
  const lines = order?.lines ?? [];
  const selectedLine = lines.find((line) => line.id === selectedLineId) ?? lines[0];
  const sizeEntries = selectedLine ? getSizeEntries(selectedLine) : [];

  const handleLineChange = (lineId: string) => {
    const line = lines.find((item) => item.id === lineId);
    form.setFieldValue('sizes', line ? createSizeQuantityMapFromLine(line) : {});
  };

  return (
    <Form<QuantityChangeFormValues> form={form} layout="vertical" preserve={false}>
      <Form.Item label="订单行" name="orderLineId" rules={[{ required: true, message: '请选择订单行' }]}>
        <Select
          aria-label="数量变更行"
          options={lines.map((line) => ({ label: formatLineLabel(line), value: line.id }))}
          onChange={handleLineChange}
        />
      </Form.Item>
      <Space wrap>
        {sizeEntries.map((size) => (
          <Form.Item key={size.sizeId} label={`变更尺码 ${size.code}`} name={['sizes', size.sizeId]}>
            <InputNumber min={0} precision={0} />
          </Form.Item>
        ))}
      </Space>
      <Form.Item label="变更原因" name="reason" normalize={normalizeTextInput} rules={[{ required: true, message: '请输入变更原因' }]}>
        <Input.TextArea aria-label="变更原因" maxLength={MAX_REMARK_LENGTH} rows={3} />
      </Form.Item>
    </Form>
  );
}

function validateConvertFormValues(values: ConvertFormValues): string | null {
  if (!values.lineIds || values.lineIds.length === 0) {
    return '请选择至少一行订单行';
  }
  if (!isValidDateFilter(values.deadlineDate ?? '')) {
    return DATE_FORMAT_ERROR_MESSAGE;
  }
  return null;
}

function toConvertPayload(order: SalesOrderRecord, values: ConvertFormValues): ConvertSalesOrderPayload {
  const skipCuttingLineIds = new Set(values.skipCuttingLineIds ?? []);
  return {
    salesOrderId: order.id,
    lines: (values.lineIds ?? []).map((lineId) => ({
      salesOrderLineId: lineId,
      skipCutting: skipCuttingLineIds.has(lineId),
    })),
    ...(values.deadlineDate ? { deadlineDate: values.deadlineDate } : {}),
    ...(values.remark ? { remark: values.remark } : {}),
  };
}

function toQuantityChangePayload(order: SalesOrderRecord, values: QuantityChangeFormValues): QuantityChangePayload {
  const line = (order.lines ?? []).find((item) => item.id === values.orderLineId);
  const sizeEntries = line ? getSizeEntries(line) : [];
  return {
    orderId: order.id,
    orderLineId: values.orderLineId ?? '',
    sizeGroupId: line ? getSizeGroupId(line) : '',
    sizes: sizeEntries.map((size) => ({
      sizeId: size.sizeId,
      code: size.code,
      quantity: Number(values.sizes?.[size.sizeId] ?? 0),
    })),
    reason: values.reason ?? '',
  };
}

function validateReturnOrderFormValues(order: SalesOrderRecord | null, values: ReturnOrderFormValues): string | null {
  if (!order) {
    return '销售订单数据不存在';
  }
  if (!values.lineIds || values.lineIds.length === 0) {
    return '请选择至少一行退货明细';
  }

  const hasQuantity = values.lineIds.some((lineId) =>
    Object.values(values.sizes?.[lineId] ?? {}).some((quantity) => Number(quantity ?? 0) > 0),
  );

  return hasQuantity ? null : '退货数量必须大于 0';
}

function toReturnOrderPayload(order: SalesOrderRecord, values: ReturnOrderFormValues): CreateReturnOrderPayload {
  const selectedLineIds = new Set(values.lineIds ?? []);
  return {
    returnType: values.returnType ?? 'CUSTOMER_REJECT',
    salesOrderId: order.id,
    salesOrderNo: order.orderNo,
    customerId: order.customerId ?? '',
    ...(values.reason ? { reason: values.reason } : {}),
    ...(values.remark ? { remark: values.remark } : {}),
    lines: (order.lines ?? [])
      .filter((line) => selectedLineIds.has(line.id))
      .map((line) => toReturnOrderLinePayload(line, values.sizes?.[line.id] ?? {})),
  };
}

function toReturnOrderLinePayload(line: SalesOrderLineRecord, quantities: Record<string, number>) {
  const sizeEntries = getSizeEntries(line).map((size) => ({
    sizeId: size.sizeId,
    code: size.code,
    quantity: Number(quantities[size.sizeId] ?? 0),
  }));
  const totalQuantity = sizeEntries.reduce((sum, size) => sum + size.quantity, 0);
  return {
    salesOrderLineId: line.id,
    spuId: line.spuId ?? '',
    colorWayId: line.colorWayId ?? '',
    sizeMatrixJson: JSON.stringify({
      sizeGroupId: getSizeGroupId(line),
      sizes: sizeEntries,
      totalQuantity,
    }),
    totalQuantity,
  };
}

function getSizeGroupId(line: SalesOrderLineRecord): string {
  const sizeMatrix = line.sizeMatrix ?? {};
  const sizeGroupId = sizeMatrix.sizeGroupId;
  return typeof sizeGroupId === 'string' ? sizeGroupId : String(sizeGroupId ?? '');
}

function getSizeEntries(line: SalesOrderLineRecord): Array<{ sizeId: string; code: string; quantity: number }> {
  const sizeMatrix = line.sizeMatrix ?? {};
  const sizes = Array.isArray(sizeMatrix.sizes) ? sizeMatrix.sizes : [];
  return sizes.map((item) => {
    const size = item as { sizeId?: string; code?: string; quantity?: number };
    return {
      sizeId: String(size.sizeId ?? ''),
      code: size.code ?? '',
      quantity: Number(size.quantity ?? 0),
    };
  }).filter((size) => size.sizeId && size.code);
}

function createSizeQuantityMapFromLine(line: SalesOrderLineRecord): Record<string, number> {
  return Object.fromEntries(getSizeEntries(line).map((size) => [size.sizeId, size.quantity]));
}

function formatLineLabel(line: SalesOrderLineRecord): string {
  const style = line.spuName || line.spuCode || '未知款式';
  const color = line.colorName || line.colorCode || '未知颜色';
  return `${line.lineNo ?? '-'} - ${style} / ${color} / ${line.totalQuantity ?? 0}`;
}

function SalesOrderForm({
  customers,
  form,
  formMode,
  seasons,
  sizeGroups,
  spus,
}: {
  customers: CustomerRecord[];
  form: ReturnType<typeof Form.useForm<SalesOrderFormValues>>[0];
  formMode: FormMode;
  seasons: SeasonRecord[];
  sizeGroups: SizeGroupRecord[];
  spus: SpuRecord[];
}) {
  return (
    <Form<SalesOrderFormValues> form={form} layout="vertical" preserve={false}>
      <Space.Compact block>
        <Form.Item label="客户" name="customerId" rules={[{ required: true, message: '请选择客户' }]} style={{ width: '50%' }}>
          <Select
            aria-label="客户"
            options={customers.map((customer) => ({ label: customer.name, value: customer.id }))}
            placeholder="请选择客户"
          />
        </Form.Item>
        <Form.Item label="季节" name="seasonId" style={{ width: '50%' }}>
          <Select
            allowClear
            aria-label="季节"
            options={seasons.map((season) => ({ label: season.name, value: season.id }))}
            placeholder="请选择季节"
          />
        </Form.Item>
      </Space.Compact>
      <Space.Compact block>
        <Form.Item label="订单日期" name="orderDate" rules={formMode === 'create' ? [{ required: true, message: '请输入订单日期' }] : []} style={{ width: '50%' }}>
          <Input disabled={formMode === 'edit'} placeholder="YYYY-MM-DD" />
        </Form.Item>
        <Form.Item label="整单交期" name="deliveryDate" style={{ width: '50%' }}>
          <Input placeholder="YYYY-MM-DD" />
        </Form.Item>
      </Space.Compact>
      <Form.Item label="备注" name="remark" normalize={normalizeTextInput}>
        <Input.TextArea rows={2} maxLength={MAX_REMARK_LENGTH} />
      </Form.Item>
      <Form.List name="lines" rules={[{ validator: validateFormLines }]}>
        {(fields, operations, meta) => (
          <Space direction="vertical" style={{ width: '100%' }}>
            {fields.map((field) => (
              <ProCard key={field.key} title={`明细行 ${field.name + 1}`} size="small" bordered>
                <SalesOrderLineForm fieldName={field.name} form={form} sizeGroups={sizeGroups} spus={spus} />
                {fields.length > 1 ? (
                  <Button danger onClick={() => operations.remove(field.name)}>
                    删除明细
                  </Button>
                ) : null}
              </ProCard>
            ))}
            <Button onClick={() => operations.add(createEmptyFormLine())}>新增明细</Button>
            {meta.errors.map((error, index) => (
              <div className="ant-form-item-explain-error" key={`${String(error)}-${index}`}>{error}</div>
            ))}
          </Space>
        )}
      </Form.List>
    </Form>
  );
}

function SalesOrderLineForm({
  fieldName,
  form,
  sizeGroups,
  spus,
}: {
  fieldName: number;
  form: ReturnType<typeof Form.useForm<SalesOrderFormValues>>[0];
  sizeGroups: SizeGroupRecord[];
  spus: SpuRecord[];
}) {
  const currentLine = Form.useWatch(['lines', fieldName], form) as SalesOrderFormLine | undefined;
  const selectedSpu = spus.find((spu) => spu.id === currentLine?.spuId);
  const selectedSizeGroup = sizeGroups.find((sizeGroup) => sizeGroup.id === currentLine?.sizeGroupId);
  const colorOptions = (selectedSpu?.colorWays ?? []).map(toColorOption);
  const sizeRecords = selectedSizeGroup?.sizes ?? [];

  const handleSpuChange = (spuId: string) => {
    const spu = spus.find((item) => item.id === spuId);
    const nextLines = [...(form.getFieldValue('lines') ?? [])] as SalesOrderFormLine[];
    nextLines[fieldName] = {
      ...nextLines[fieldName],
      spuId,
      colorWayId: undefined,
      sizeGroupId: spu?.sizeGroupId,
      sizes: createSizeQuantityMap(sizeGroups.find((sizeGroup) => sizeGroup.id === spu?.sizeGroupId)),
    };
    form.setFieldValue('lines', nextLines);
  };

  return (
    <Space direction="vertical" style={{ width: '100%' }}>
      <Space.Compact block>
        <Form.Item label="款式" name={[fieldName, 'spuId']} rules={[{ required: true, message: '请选择款式' }]} style={{ width: '50%' }}>
          <Select aria-label="款式" options={spus.map(toSpuOption)} placeholder="请选择款式" onChange={handleSpuChange} />
        </Form.Item>
        <Form.Item label="颜色" name={[fieldName, 'colorWayId']} rules={[{ required: true, message: '请选择颜色' }]} style={{ width: '50%' }}>
          <Select aria-label="颜色" options={colorOptions} placeholder="请选择颜色" />
        </Form.Item>
      </Space.Compact>
      <Form.Item label="尺码组" name={[fieldName, 'sizeGroupId']} hidden>
        <Input />
      </Form.Item>
      <Space wrap>
        {sizeRecords.map((size) => (
          <Form.Item key={size.id} label={`尺码 ${size.code}`} name={[fieldName, 'sizes', size.id]} initialValue={0}>
            <InputNumber min={0} precision={0} />
          </Form.Item>
        ))}
      </Space>
      <Space.Compact block>
        <Form.Item label="单价" name={[fieldName, 'unitPrice']} initialValue={DEFAULT_UNIT_PRICE} style={{ width: '50%' }}>
          <InputNumber min={0} precision={2} style={{ width: '100%' }} />
        </Form.Item>
        <Form.Item label="折扣率" name={[fieldName, 'discountRate']} initialValue={DEFAULT_DISCOUNT_RATE} style={{ width: '50%' }}>
          <InputNumber min={0} max={1} precision={2} style={{ width: '100%' }} />
        </Form.Item>
      </Space.Compact>
      <Space.Compact block>
        <Form.Item label="行交期" name={[fieldName, 'deliveryDate']} normalize={normalizeTextInput} style={{ width: '50%' }}>
          <Input placeholder="YYYY-MM-DD" />
        </Form.Item>
        <Form.Item label="行备注" name={[fieldName, 'remark']} normalize={normalizeTextInput} style={{ width: '50%' }}>
          <Input allowClear />
        </Form.Item>
      </Space.Compact>
    </Space>
  );
}

function createEmptyFormLine(): SalesOrderFormLine {
  return {
    unitPrice: DEFAULT_UNIT_PRICE,
    discountRate: DEFAULT_DISCOUNT_RATE,
    sizes: {},
  };
}

function toFormValues(order: SalesOrderRecord): SalesOrderFormValues {
  return {
    customerId: order.customerId ?? undefined,
    seasonId: order.seasonId ?? undefined,
    orderDate: order.orderDate ?? undefined,
    deliveryDate: order.deliveryDate ?? undefined,
    remark: order.remark ?? undefined,
    lines: (order.lines ?? []).map(toFormLine),
  };
}

function toFormLine(line: SalesOrderLineRecord): SalesOrderFormLine {
  const sizeMatrix = line.sizeMatrix ?? {};
  const sizes = Array.isArray(sizeMatrix.sizes) ? sizeMatrix.sizes : [];
  return {
    spuId: line.spuId ?? undefined,
    colorWayId: line.colorWayId ?? undefined,
    sizeGroupId: typeof sizeMatrix.sizeGroupId === 'string' ? sizeMatrix.sizeGroupId : String(sizeMatrix.sizeGroupId ?? ''),
    sizes: Object.fromEntries(sizes.map((item) => {
      const size = item as { sizeId?: string; code?: string; quantity?: number };
      return [String(size.sizeId), size.quantity ?? 0];
    })),
    unitPrice: line.unitPrice ?? DEFAULT_UNIT_PRICE,
    discountRate: line.discountRate ?? DEFAULT_DISCOUNT_RATE,
    deliveryDate: line.deliveryDate ?? undefined,
    remark: line.remark ?? undefined,
  };
}

function toSavePayload(values: SalesOrderFormValues, includeOrderDate: boolean, sizeGroups: SizeGroupRecord[]): SaveSalesOrderPayload {
  return {
    customerId: values.customerId ?? '',
    ...(includeOrderDate ? { orderDate: values.orderDate } : {}),
    ...(values.seasonId ? { seasonId: values.seasonId } : {}),
    ...(values.deliveryDate ? { deliveryDate: values.deliveryDate } : {}),
    ...(values.remark ? { remark: values.remark } : {}),
    lines: (values.lines ?? []).map((line) => toSaveLine(line, sizeGroups)),
  };
}

function toSaveLine(line: SalesOrderFormLine, sizeGroups: SizeGroupRecord[]) {
  const sizeGroup = sizeGroups.find((item) => item.id === line.sizeGroupId);
  return {
    spuId: line.spuId ?? '',
    colorWayId: line.colorWayId ?? '',
    sizeGroupId: line.sizeGroupId ?? '',
    sizes: (sizeGroup?.sizes ?? []).map((size) => ({
      sizeId: size.id,
      code: size.code,
      quantity: Number(line.sizes?.[size.id] ?? 0),
    })),
    unitPrice: line.unitPrice,
    discountRate: line.discountRate,
    ...(line.deliveryDate ? { deliveryDate: line.deliveryDate } : {}),
    ...(line.remark ? { remark: line.remark } : {}),
  };
}

function validateOrderFormValues(values: SalesOrderFormValues): string | null {
  if (!isValidDateFilter(values.orderDate ?? '') || !isValidDateFilter(values.deliveryDate ?? '')) {
    return DATE_FORMAT_ERROR_MESSAGE;
  }

  const invalidLineDate = (values.lines ?? []).some((line) => !isValidDateFilter(line.deliveryDate ?? ''));
  if (invalidLineDate) {
    return DATE_FORMAT_ERROR_MESSAGE;
  }

  const hasQuantity = (values.lines ?? []).some((line) =>
    Object.values(line.sizes ?? {}).some((quantity) => Number(quantity ?? 0) > 0),
  );
  return hasQuantity ? null : '订单至少需要一个大于 0 的尺码数量';
}

async function validateFormLines(_: unknown, value?: SalesOrderFormLine[]) {
  if (!value || value.length === 0) {
    throw new Error('订单至少需要一行明细');
  }
}

function createSizeQuantityMap(sizeGroup?: SizeGroupRecord): Record<string, number> {
  return Object.fromEntries((sizeGroup?.sizes ?? []).map((size) => [size.id, 0]));
}

function toSpuOption(spu: SpuRecord) {
  return {
    label: `${spu.code} ${spu.name}`,
    value: spu.id,
  };
}

function toColorOption(color: ColorWayRecord) {
  return {
    label: `${color.colorName} / ${color.colorCode}`,
    value: color.id,
  };
}

function normalizeTextInput(value?: string): string | undefined {
  return value?.trim();
}

function isFormValidationError(error: unknown): boolean {
  return typeof error === 'object' && error !== null && 'errorFields' in error;
}
