import { EditOutlined, EyeOutlined, PlusOutlined, ReloadOutlined, SearchOutlined } from '@ant-design/icons';
import { ProCard } from '@ant-design/pro-components';
import { App, Button, Descriptions, Form, Input, InputNumber, Modal, Select, Space, Table, Tag } from 'antd';
import type { ColumnsType, TablePaginationConfig } from 'antd/es/table';
import { useCallback, useEffect, useState } from 'react';
import { EmptyState, ErrorState, LoadingState } from '@/components/state';
import { getCurrentUserPermissions } from '@/services/auth/authService';
import { getApiErrorMessage } from '@/services/http/apiClient';
import type { PageResult } from '@/services/master/customerService';
import {
  createProcurementOrder,
  fireProcurementOrderEvent,
  getProcurementOrderAvailableActions,
  getProcurementOrderDetail,
  pageProcurementOrders,
  updateProcurementOrder,
  type CreateProcurementOrderPayload,
  type ProcurementOrderLineRecord,
  type ProcurementOrderRecord,
  type UpdateProcurementOrderPayload,
} from '@/services/procurement/procurementService';
import { getAuthSession, setAuthSession } from '@/shared/storage/authSessionStorage';

const DEFAULT_PAGE_SIZE = 10;
const INITIAL_PAGE = 1;

const statusOptions = [
  { label: '全部状态', value: '' },
  { label: '草稿', value: 'DRAFT' },
  { label: '待审批', value: 'PENDING_APPROVAL' },
  { label: '已审批', value: 'APPROVED' },
  { label: '已驳回', value: 'REJECTED' },
  { label: '已下发', value: 'ISSUED' },
  { label: '收货中', value: 'RECEIVING' },
  { label: '已完成', value: 'COMPLETED' },
];

const actionLabelMap: Record<string, string> = {
  APPROVE: '审批通过',
  COMPLETE: '完成采购',
  ISSUE: '下发采购',
  RECEIVE: '开始收货',
  REJECT: '审批驳回',
  RESUBMIT: '重新提交',
  SUBMIT: '提交审批',
};

const statusColorMap: Record<string, string> = {
  APPROVED: 'green',
  COMPLETED: 'green',
  DRAFT: 'default',
  ISSUED: 'blue',
  PENDING_APPROVAL: 'gold',
  RECEIVING: 'cyan',
  REJECTED: 'red',
};

export function ProcurementOrderListPage() {
  const { message } = App.useApp();
  const [form] = Form.useForm<ProcurementOrderFormValues>();
  const [supplierInput, setSupplierInput] = useState('');
  const [supplierId, setSupplierId] = useState('');
  const [status, setStatus] = useState('');
  const [currentPage, setCurrentPage] = useState(INITIAL_PAGE);
  const [pageSize, setPageSize] = useState(DEFAULT_PAGE_SIZE);
  const [loading, setLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState('');
  const [pageResult, setPageResult] = useState<PageResult<ProcurementOrderRecord> | null>(null);
  const [detailOpen, setDetailOpen] = useState(false);
  const [detailLoading, setDetailLoading] = useState(false);
  const [detail, setDetail] = useState<ProcurementOrderRecord | null>(null);
  const [availableActions, setAvailableActions] = useState<string[]>([]);
  const [permissions, setPermissions] = useState<string[]>(() => getAuthSession()?.permissions ?? []);
  const [formOpen, setFormOpen] = useState(false);
  const [saving, setSaving] = useState(false);
  const canCreate = permissions.includes('procurement:order:create');
  const canUpdate = permissions.includes('procurement:order:update');
  const canFireEvent = permissions.includes('procurement:order:fire-event');
  const [editingId, setEditingId] = useState<string | null>(null);

  const loadOrders = useCallback(async () => {
    setLoading(true);
    setErrorMessage('');
    try {
      setPageResult(await pageProcurementOrders({
        current: currentPage,
        size: pageSize,
        ...(supplierId ? { supplierId } : {}),
        ...(status ? { status } : {}),
      }));
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error));
    } finally {
      setLoading(false);
    }
  }, [currentPage, pageSize, supplierId, status]);

  useEffect(() => {
    void loadOrders();
  }, [loadOrders]);

  useEffect(() => {
    void refreshPermissions();
  }, []);

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

  const handleSearch = () => {
    setSupplierId(supplierInput.trim());
    setCurrentPage(INITIAL_PAGE);
  };

  async function openDetail(record: ProcurementOrderRecord) {
    setDetailOpen(true);
    setDetailLoading(true);
    try {
      const [nextDetail, nextActions] = await Promise.all([
        getProcurementOrderDetail(record.id),
        getProcurementOrderAvailableActions(record.id),
      ]);
      setDetail(nextDetail);
      setAvailableActions(nextActions);
    } catch (error) {
      message.error(getApiErrorMessage(error));
      setDetailOpen(false);
    } finally {
      setDetailLoading(false);
    }
  }

  async function handleFireEvent(event: string) {
    if (!detail) {
      return;
    }
    try {
      await fireProcurementOrderEvent({ orderId: detail.id, event });
      message.success(`${getActionLabel(event)}成功`);
      const [nextDetail, nextActions] = await Promise.all([
        getProcurementOrderDetail(detail.id),
        getProcurementOrderAvailableActions(detail.id),
      ]);
      setDetail(nextDetail);
      setAvailableActions(nextActions);
      await loadOrders();
    } catch (error) {
      message.error(getApiErrorMessage(error));
    }
  }

  function openCreateForm() {
    form.setFieldsValue({ lines: [createEmptyOrderLineForm()] });
    setFormOpen(true);
  }

  async function handleCreateOrder() {
    try {
      const values = await form.validateFields();
      const payload = toProcurementOrderPayload(values);
      setSaving(true);
      await createProcurementOrder(payload);
      message.success('新增采购订单成功');
      setFormOpen(false);
      await loadOrders();
    } catch (error) {
      if (isFormValidationError(error)) {
        return;
      }
      message.error(getApiErrorMessage(error));
    } finally {
      setSaving(false);
    }
  }

  async function openEditForm(record: ProcurementOrderRecord) {
    try {
      setDetailLoading(true);
      const detail = await getProcurementOrderDetail(record.id);
      setEditingId(record.id);
      form.setFieldsValue({
        supplierId: detail.supplierId ?? undefined,
        orderDate: detail.orderDate ?? undefined,
        expectedDeliveryDate: detail.expectedDeliveryDate ?? undefined,
        remark: detail.remark ?? undefined,
        lines: (detail.lines ?? []).map((line) => ({
          materialId: line.materialId ?? undefined,
          materialType: line.materialType ?? undefined,
          quantity: line.quantity ?? undefined,
          unit: line.unit ?? undefined,
          unitPrice: line.unitPrice ?? undefined,
          mrpResultId: line.mrpResultId ?? undefined,
          remark: line.remark ?? undefined,
        })),
      });
      setFormOpen(true);
    } catch (error) {
      message.error(getApiErrorMessage(error));
    } finally {
      setDetailLoading(false);
    }
  }

  async function handleUpdateOrder() {
    if (!editingId) return;
    try {
      const values = await form.validateFields();
      const payload = toUpdateProcurementOrderPayload(values);
      setSaving(true);
      await updateProcurementOrder(editingId, payload);
      message.success('更新采购订单成功');
      setFormOpen(false);
      setEditingId(null);
      await loadOrders();
    } catch (error) {
      if (isFormValidationError(error)) {
        return;
      }
      message.error(getApiErrorMessage(error));
    } finally {
      setSaving(false);
    }
  }

  if (loading && !pageResult) {
    return <LoadingState message="正在加载采购订单" />;
  }

  if (errorMessage && !pageResult) {
    return <ErrorState message={errorMessage} onRetry={loadOrders} />;
  }

  return (
    <div className="system-page procurement-order-list-page">
      <section className="system-page-topbar">
        <div>
          <h1>采购订单</h1>
          <p>跟踪采购单审批、下发、收货和完成状态。</p>
        </div>
      </section>

      <ProCard className="system-page-card" bordered={false}>
        <Space className="system-page-toolbar" wrap>
          <Input placeholder="供应商ID" value={supplierInput} onChange={(event) => setSupplierInput(event.target.value)} />
          <Select aria-label="采购订单状态筛选" options={statusOptions} value={status} onChange={(value) => { setStatus(value); setCurrentPage(INITIAL_PAGE); }} />
          <Button icon={<SearchOutlined />} onClick={handleSearch}>搜索</Button>
          <Button icon={<ReloadOutlined />} onClick={loadOrders}>刷新</Button>
          {canCreate ? (
            <Button aria-label="新增采购订单" icon={<PlusOutlined />} type="primary" onClick={openCreateForm}>新增采购订单</Button>
          ) : null}
        </Space>
        {pageResult?.records.length === 0 ? (
          <EmptyState message="暂无采购订单" />
        ) : (
          <Table<ProcurementOrderRecord>
            rowKey="id"
            columns={buildColumns(openDetail, canUpdate ? openEditForm : undefined)}
            dataSource={pageResult?.records ?? []}
            loading={loading}
            pagination={{
              current: Number(pageResult?.current ?? currentPage),
              pageSize: Number(pageResult?.size ?? pageSize),
              total: Number(pageResult?.total ?? 0),
              showSizeChanger: true,
            }}
            onChange={(pagination: TablePaginationConfig) => {
              setCurrentPage(pagination.current ?? INITIAL_PAGE);
              setPageSize(pagination.pageSize ?? DEFAULT_PAGE_SIZE);
            }}
          />
        )}
      </ProCard>

      <ProcurementOrderDetailModal
        availableActions={availableActions}
        canFireEvent={canFireEvent}
        detail={detail}
        loading={detailLoading}
        open={detailOpen}
        onCancel={() => setDetailOpen(false)}
        onFireEvent={handleFireEvent}
      />
      <ProcurementOrderFormModal
        form={form}
        isEdit={!!editingId}
        open={formOpen}
        saving={saving}
        onCancel={() => { setFormOpen(false); setEditingId(null); }}
        onSave={editingId ? handleUpdateOrder : handleCreateOrder}
      />
    </div>
  );
}

function ProcurementOrderFormModal({
  form,
  isEdit,
  open,
  saving,
  onCancel,
  onSave,
}: {
  form: ReturnType<typeof Form.useForm<ProcurementOrderFormValues>>[0];
  isEdit: boolean;
  open: boolean;
  saving: boolean;
  onCancel: () => void;
  onSave: () => void;
}) {
  return (
    <Modal
      cancelText="取消"
      confirmLoading={saving}
      getContainer={false}
      okText={isEdit ? '更新采购订单' : '保存采购订单'}
      onCancel={onCancel}
      onOk={onSave}
      open={open}
      title={isEdit ? '编辑采购订单' : '新增采购订单'}
      width={980}
    >
      <Form<ProcurementOrderFormValues> form={form} layout="vertical">
        <Space align="start" style={{ width: '100%' }} wrap>
          <Form.Item label="供应商ID" name="supplierId" rules={[{ required: true, message: '请输入供应商ID' }]}>
            <Input />
          </Form.Item>
          <Form.Item label="订单日期" name="orderDate">
            <Input placeholder="YYYY-MM-DD" />
          </Form.Item>
          <Form.Item label="要求交货日期" name="expectedDeliveryDate">
            <Input placeholder="YYYY-MM-DD" />
          </Form.Item>
        </Space>
        <Form.Item label="订单备注" name="remark">
          <Input.TextArea autoSize={{ minRows: 2, maxRows: 4 }} />
        </Form.Item>
        <Form.List name="lines" rules={[{ validator: validateOrderLines }]}>
          {(fields, { add, remove }) => (
            <Space direction="vertical" style={{ width: '100%' }}>
              {fields.map((field) => (
                <ProCard key={field.key} bordered size="small">
                  <Space align="start" wrap>
                    <Form.Item label="物料ID" name={[field.name, 'materialId']} rules={[{ required: true, message: '请输入物料ID' }]}>
                      <Input />
                    </Form.Item>
                    <Form.Item label="物料类型" name={[field.name, 'materialType']}>
                      <Input placeholder="FABRIC/TRIM/PACKAGING" />
                    </Form.Item>
                    <Form.Item label="采购数量" name={[field.name, 'quantity']} rules={[{ required: true, message: '请输入采购数量' }]}>
                      <InputNumber min={0} />
                    </Form.Item>
                    <Form.Item label="单位" name={[field.name, 'unit']}>
                      <Input />
                    </Form.Item>
                    <Form.Item label="单价" name={[field.name, 'unitPrice']}>
                      <InputNumber min={0} />
                    </Form.Item>
                    <Form.Item label="MRP结果ID" name={[field.name, 'mrpResultId']}>
                      <Input />
                    </Form.Item>
                    <Form.Item label="行备注" name={[field.name, 'remark']}>
                      <Input />
                    </Form.Item>
                    {fields.length > 1 ? (
                      <Button danger onClick={() => remove(field.name)}>删除行</Button>
                    ) : null}
                  </Space>
                </ProCard>
              ))}
              <Button onClick={() => add(createEmptyOrderLineForm())}>新增采购行</Button>
            </Space>
          )}
        </Form.List>
      </Form>
    </Modal>
  );
}

function buildColumns(
  onDetail: (record: ProcurementOrderRecord) => void,
  onEdit?: (record: ProcurementOrderRecord) => void,
): ColumnsType<ProcurementOrderRecord> {
  return [
    { title: '采购单号', dataIndex: 'orderNo', key: 'orderNo', width: 170 },
    { title: '供应商', dataIndex: 'supplierName', key: 'supplierName', render: (value) => value || '-' },
    { title: '订单日期', dataIndex: 'orderDate', key: 'orderDate', width: 120, render: (value) => value || '-' },
    { title: '交货日期', dataIndex: 'expectedDeliveryDate', key: 'expectedDeliveryDate', width: 120, render: (value) => value || '-' },
    {
      title: '状态',
      key: 'status',
      width: 120,
      render: (_, record) => <Tag color={statusColorMap[record.status ?? ''] ?? 'default'}>{record.statusLabel || record.status || '-'}</Tag>,
    },
    { title: '总金额', dataIndex: 'totalAmount', key: 'totalAmount', width: 120, render: formatAmount },
    {
      title: '操作',
      key: 'actions',
      width: 160,
      render: (_, record) => (
        <Space>
          <Button icon={<EyeOutlined />} size="small" aria-label={`详情 ${record.orderNo}`} onClick={() => onDetail(record)}>
            详情
          </Button>
          {onEdit && record.status === 'DRAFT' ? (
            <Button icon={<EditOutlined />} size="small" aria-label={`编辑 ${record.orderNo}`} onClick={() => onEdit(record)}>
              编辑
            </Button>
          ) : null}
        </Space>
      ),
    },
  ];
}

function ProcurementOrderDetailModal({
  availableActions,
  canFireEvent,
  detail,
  loading,
  open,
  onCancel,
  onFireEvent,
}: {
  availableActions: string[];
  canFireEvent: boolean;
  detail: ProcurementOrderRecord | null;
  loading: boolean;
  open: boolean;
  onCancel: () => void;
  onFireEvent: (event: string) => void;
}) {
  return (
    <Modal footer={null} getContainer={false} onCancel={onCancel} open={open} title={detail?.orderNo ?? '采购订单详情'} width={980}>
      <ProCard loading={loading} bordered={false}>
        {detail ? (
          <Space direction="vertical" style={{ width: '100%' }}>
            <Descriptions column={3} size="small">
              <Descriptions.Item label="采购单号">{detail.orderNo}</Descriptions.Item>
              <Descriptions.Item label="供应商">{detail.supplierName || '-'}</Descriptions.Item>
              <Descriptions.Item label="状态">{detail.statusLabel || detail.status || '-'}</Descriptions.Item>
              <Descriptions.Item label="订单日期">{detail.orderDate || '-'}</Descriptions.Item>
              <Descriptions.Item label="交货日期">{detail.expectedDeliveryDate || '-'}</Descriptions.Item>
              <Descriptions.Item label="总金额">{formatAmount(detail.totalAmount)}</Descriptions.Item>
              <Descriptions.Item label="MRP批次">{detail.mrpBatchNo || '-'}</Descriptions.Item>
              <Descriptions.Item label="付款状态">{detail.paymentStatus || '-'}</Descriptions.Item>
              <Descriptions.Item label="备注">{detail.remark || '-'}</Descriptions.Item>
            </Descriptions>
            {canFireEvent && availableActions.length > 0 ? (
              <Space wrap>
                {availableActions.map((event) => (
                  <Button key={event} type="primary" onClick={() => onFireEvent(event)}>
                    {getActionLabel(event)}
                  </Button>
                ))}
              </Space>
            ) : null}
            <Table<ProcurementOrderLineRecord>
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
  );
}

const lineColumns: ColumnsType<ProcurementOrderLineRecord> = [
  { title: '行号', dataIndex: 'lineNo', key: 'lineNo', width: 70 },
  { title: '物料编码', dataIndex: 'materialCode', key: 'materialCode', width: 120, render: (value) => value || '-' },
  { title: '物料名称', dataIndex: 'materialName', key: 'materialName', render: (value) => value || '-' },
  { title: '数量', dataIndex: 'quantity', key: 'quantity', width: 90, render: formatQuantity },
  { title: '单位', dataIndex: 'unit', key: 'unit', width: 70, render: (value) => value || '-' },
  { title: '单价', dataIndex: 'unitPrice', key: 'unitPrice', width: 90, render: formatAmount },
  { title: '金额', dataIndex: 'lineAmount', key: 'lineAmount', width: 100, render: formatAmount },
  { title: '已到货', dataIndex: 'deliveredQuantity', key: 'deliveredQuantity', width: 90, render: formatQuantity },
  { title: '合格', dataIndex: 'acceptedQuantity', key: 'acceptedQuantity', width: 80, render: formatQuantity },
  { title: '不合格', dataIndex: 'rejectedQuantity', key: 'rejectedQuantity', width: 90, render: formatQuantity },
];

function getActionLabel(event: string): string {
  return actionLabelMap[event] ?? event;
}

interface ProcurementOrderLineFormValues {
  materialId?: string;
  materialType?: string;
  mrpResultId?: string;
  quantity?: number | null;
  remark?: string;
  unit?: string;
  unitPrice?: number | null;
}

interface ProcurementOrderFormValues {
  expectedDeliveryDate?: string;
  lines?: ProcurementOrderLineFormValues[];
  orderDate?: string;
  remark?: string;
  supplierId?: string;
}

function createEmptyOrderLineForm(): ProcurementOrderLineFormValues {
  return {
    materialType: 'FABRIC',
    unit: '米',
  };
}

function toProcurementOrderPayload(values: ProcurementOrderFormValues): CreateProcurementOrderPayload {
  return {
    supplierId: trimOptional(values.supplierId) ?? '',
    orderDate: trimOptional(values.orderDate),
    expectedDeliveryDate: trimOptional(values.expectedDeliveryDate),
    remark: trimOptional(values.remark),
    lines: (values.lines ?? []).map((line) => ({
      materialId: trimOptional(line.materialId) ?? '',
      materialType: trimOptional(line.materialType),
      quantity: Number(line.quantity ?? 0),
      unit: trimOptional(line.unit),
      unitPrice: typeof line.unitPrice === 'number' ? line.unitPrice : undefined,
      mrpResultId: trimOptional(line.mrpResultId),
      remark: trimOptional(line.remark),
    })),
  };
}

function toUpdateProcurementOrderPayload(values: ProcurementOrderFormValues): UpdateProcurementOrderPayload {
  return {
    supplierId: trimOptional(values.supplierId) ?? '',
    orderDate: trimOptional(values.orderDate),
    expectedDeliveryDate: trimOptional(values.expectedDeliveryDate),
    remark: trimOptional(values.remark),
    lines: (values.lines ?? []).map((line) => ({
      materialId: trimOptional(line.materialId) ?? '',
      materialType: trimOptional(line.materialType),
      quantity: Number(line.quantity ?? 0),
      unit: trimOptional(line.unit),
      unitPrice: typeof line.unitPrice === 'number' ? line.unitPrice : undefined,
      mrpResultId: trimOptional(line.mrpResultId),
      remark: trimOptional(line.remark),
    })),
  };
}

async function validateOrderLines(_: unknown, value?: ProcurementOrderLineFormValues[]) {
  if (!value || value.length === 0) {
    throw new Error('至少需要一行采购明细');
  }
}

function trimOptional(value?: string | null): string | undefined {
  const nextValue = value?.trim();
  return nextValue ? nextValue : undefined;
}

function isFormValidationError(error: unknown): boolean {
  return typeof error === 'object' && error !== null && 'errorFields' in error;
}

function formatAmount(value?: number | null): string {
  return typeof value === 'number'
    ? value.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
    : '0.00';
}

function formatQuantity(value?: number | null): string {
  return typeof value === 'number' ? value.toLocaleString('zh-CN') : '0';
}
