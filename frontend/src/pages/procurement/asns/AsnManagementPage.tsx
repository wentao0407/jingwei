import { EyeOutlined, PlusOutlined, ReloadOutlined, SearchOutlined } from '@ant-design/icons';
import { ProCard } from '@ant-design/pro-components';
import { App, Button, Descriptions, Form, Input, InputNumber, Modal, Select, Space, Table, Tag } from 'antd';
import type { ColumnsType, TablePaginationConfig } from 'antd/es/table';
import { useCallback, useEffect, useState } from 'react';
import { EmptyState, ErrorState, LoadingState } from '@/components/state';
import { getCurrentUserPermissions } from '@/services/auth/authService';
import { getApiErrorMessage } from '@/services/http/apiClient';
import type { PageResult } from '@/services/master/customerService';
import {
  createAsn,
  getAsnDetail,
  pageAsns,
  receiveAsnGoods,
  submitAsnQc,
  type AsnLineRecord,
  type AsnRecord,
  type CreateAsnPayload,
} from '@/services/procurement/procurementService';
import { getAuthSession, setAuthSession } from '@/shared/storage/authSessionStorage';

const DEFAULT_PAGE_SIZE = 10;
const INITIAL_PAGE = 1;

const statusOptions = [
  { label: '全部状态', value: '' },
  { label: '待到货', value: 'PENDING' },
  { label: '部分收货', value: 'PARTIAL_RECEIVED' },
  { label: '已收货', value: 'RECEIVED' },
  { label: '已关闭', value: 'CLOSED' },
];

const statusColorMap: Record<string, string> = {
  CLOSED: 'default',
  PARTIAL_RECEIVED: 'cyan',
  PENDING: 'gold',
  RECEIVED: 'green',
};

export function AsnManagementPage() {
  const { message } = App.useApp();
  const [orderInput, setOrderInput] = useState('');
  const [procurementOrderId, setProcurementOrderId] = useState('');
  const [status, setStatus] = useState('');
  const [currentPage, setCurrentPage] = useState(INITIAL_PAGE);
  const [pageSize, setPageSize] = useState(DEFAULT_PAGE_SIZE);
  const [loading, setLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState('');
  const [pageResult, setPageResult] = useState<PageResult<AsnRecord> | null>(null);
  const [detailOpen, setDetailOpen] = useState(false);
  const [detailLoading, setDetailLoading] = useState(false);
  const [detail, setDetail] = useState<AsnRecord | null>(null);
  const [receiveLine, setReceiveLine] = useState<AsnLineRecord | null>(null);
  const [qcLine, setQcLine] = useState<AsnLineRecord | null>(null);
  const [permissions, setPermissions] = useState<string[]>(() => getAuthSession()?.permissions ?? []);
  const [createForm] = Form.useForm<AsnFormValues>();
  const [createOpen, setCreateOpen] = useState(false);
  const [saving, setSaving] = useState(false);
  const actions = {
    canCreate: permissions.includes('procurement:asn:create'),
    canReceive: permissions.includes('procurement:asn:receive'),
    canQc: permissions.includes('procurement:asn:qc'),
  };

  const loadAsns = useCallback(async () => {
    setLoading(true);
    setErrorMessage('');
    try {
      setPageResult(await pageAsns({
        current: currentPage,
        size: pageSize,
        ...(procurementOrderId ? { procurementOrderId } : {}),
        ...(status ? { status } : {}),
      }));
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error));
    } finally {
      setLoading(false);
    }
  }, [currentPage, pageSize, procurementOrderId, status]);

  useEffect(() => {
    void loadAsns();
  }, [loadAsns]);

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

  async function openDetail(record: AsnRecord) {
    setDetailOpen(true);
    setDetailLoading(true);
    try {
      setDetail(await getAsnDetail(record.id));
    } catch (error) {
      message.error(getApiErrorMessage(error));
      setDetailOpen(false);
    } finally {
      setDetailLoading(false);
    }
  }

  async function reloadDetail() {
    if (!detail) {
      return;
    }
    setDetail(await getAsnDetail(detail.id));
    await loadAsns();
  }

  function openCreateForm() {
    createForm.setFieldsValue({ lines: [createEmptyAsnLineForm()] });
    setCreateOpen(true);
  }

  async function handleCreateAsn() {
    try {
      const values = await createForm.validateFields();
      setSaving(true);
      await createAsn(toAsnPayload(values));
      message.success('新增ASN成功');
      setCreateOpen(false);
      await loadAsns();
    } catch (error) {
      if (!isFormValidationError(error)) {
        message.error(getApiErrorMessage(error));
      }
    } finally {
      setSaving(false);
    }
  }

  if (loading && !pageResult) {
    return <LoadingState message="正在加载到货通知" />;
  }

  if (errorMessage && !pageResult) {
    return <ErrorState message={errorMessage} onRetry={loadAsns} />;
  }

  return (
    <div className="system-page asn-management-page">
      <section className="system-page-topbar">
        <div>
          <h1>到货通知</h1>
          <p>跟踪 ASN 到货、收货和质检状态。</p>
        </div>
      </section>

      <ProCard className="system-page-card" bordered={false}>
        <Space className="system-page-toolbar" wrap>
          <Input placeholder="采购订单ID" value={orderInput} onChange={(event) => setOrderInput(event.target.value)} />
          <Select aria-label="到货状态筛选" options={statusOptions} value={status} onChange={(value) => { setStatus(value); setCurrentPage(INITIAL_PAGE); }} />
          <Button icon={<SearchOutlined />} onClick={() => { setProcurementOrderId(orderInput.trim()); setCurrentPage(INITIAL_PAGE); }}>搜索</Button>
          <Button icon={<ReloadOutlined />} onClick={loadAsns}>刷新</Button>
          {actions.canCreate ? (
            <Button aria-label="新增ASN" icon={<PlusOutlined />} type="primary" onClick={openCreateForm}>新增ASN</Button>
          ) : null}
        </Space>
        {pageResult?.records.length === 0 ? (
          <EmptyState message="暂无到货通知" />
        ) : (
          <Table<AsnRecord>
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
            onChange={(pagination: TablePaginationConfig) => {
              setCurrentPage(pagination.current ?? INITIAL_PAGE);
              setPageSize(pagination.pageSize ?? DEFAULT_PAGE_SIZE);
            }}
          />
        )}
      </ProCard>

      <AsnDetailModal
        actions={actions}
        detail={detail}
        loading={detailLoading}
        open={detailOpen}
        onCancel={() => setDetailOpen(false)}
        onOpenQc={setQcLine}
        onOpenReceive={setReceiveLine}
      />
      <ReceiveModal asn={detail} line={receiveLine} onClose={() => setReceiveLine(null)} onReload={reloadDetail} />
      <QcModal line={qcLine} onClose={() => setQcLine(null)} onReload={reloadDetail} />
      <AsnCreateModal
        form={createForm}
        open={createOpen}
        saving={saving}
        onCancel={() => setCreateOpen(false)}
        onSave={handleCreateAsn}
      />
    </div>
  );
}

function AsnCreateModal({
  form,
  open,
  saving,
  onCancel,
  onSave,
}: {
  form: ReturnType<typeof Form.useForm<AsnFormValues>>[0];
  open: boolean;
  saving: boolean;
  onCancel: () => void;
  onSave: () => void;
}) {
  return (
    <Modal cancelText="取消" confirmLoading={saving} getContainer={false} okText="保存ASN" onCancel={onCancel} onOk={onSave} open={open} title="新增ASN" width={980}>
      <Form<AsnFormValues> form={form} layout="vertical">
        <Space align="start" wrap>
          <Form.Item label="采购订单ID" name="procurementOrderId" rules={[{ required: true, message: '请输入采购订单ID' }]}><Input /></Form.Item>
          <Form.Item label="供应商ID" name="supplierId" rules={[{ required: true, message: '请输入供应商ID' }]}><Input /></Form.Item>
          <Form.Item label="预计到货日期" name="expectedArrivalDate"><Input placeholder="YYYY-MM-DD" /></Form.Item>
        </Space>
        <Form.Item label="备注" name="remark"><Input.TextArea rows={2} /></Form.Item>
        <Form.List name="lines" rules={[{ validator: validateAsnLines }]}>
          {(fields, { add, remove }) => (
            <Space direction="vertical" style={{ width: '100%' }}>
              {fields.map((field) => (
                <ProCard key={field.key} bordered size="small">
                  <Space align="start" wrap>
                    <Form.Item label="采购订单行ID" name={[field.name, 'procurementLineId']} rules={[{ required: true, message: '请输入采购订单行ID' }]}><Input /></Form.Item>
                    <Form.Item label="物料ID" name={[field.name, 'materialId']} rules={[{ required: true, message: '请输入物料ID' }]}><Input /></Form.Item>
                    <Form.Item label="预计到货数量" name={[field.name, 'expectedQuantity']} rules={[{ required: true, message: '请输入预计到货数量' }]}><InputNumber min={0} precision={2} /></Form.Item>
                    <Form.Item label="批次号" name={[field.name, 'batchNo']}><Input /></Form.Item>
                    <Form.Item label="行备注" name={[field.name, 'remark']}><Input /></Form.Item>
                    {fields.length > 1 ? <Button danger onClick={() => remove(field.name)}>删除行</Button> : null}
                  </Space>
                </ProCard>
              ))}
              <Button onClick={() => add(createEmptyAsnLineForm())}>新增ASN行</Button>
            </Space>
          )}
        </Form.List>
      </Form>
    </Modal>
  );
}

function buildColumns(onDetail: (record: AsnRecord) => void): ColumnsType<AsnRecord> {
  return [
    { title: 'ASN单号', dataIndex: 'asnNo', key: 'asnNo', width: 170 },
    { title: '采购单号', dataIndex: 'procurementOrderNo', key: 'procurementOrderNo', width: 170, render: (value) => value || '-' },
    { title: '供应商', dataIndex: 'supplierName', key: 'supplierName', render: (value) => value || '-' },
    { title: '预计到货', dataIndex: 'expectedArrivalDate', key: 'expectedArrivalDate', width: 120, render: (value) => value || '-' },
    {
      title: '状态',
      key: 'status',
      width: 120,
      render: (_, record) => <Tag color={statusColorMap[record.status ?? ''] ?? 'default'}>{record.statusLabel || record.status || '-'}</Tag>,
    },
    {
      title: '操作',
      key: 'actions',
      width: 120,
      render: (_, record) => (
        <Button icon={<EyeOutlined />} size="small" aria-label={`详情 ${record.asnNo}`} onClick={() => onDetail(record)}>
          详情
        </Button>
      ),
    },
  ];
}

function AsnDetailModal({
  actions,
  detail,
  loading,
  open,
  onCancel,
  onOpenQc,
  onOpenReceive,
}: {
  actions: { canReceive: boolean; canQc: boolean };
  detail: AsnRecord | null;
  loading: boolean;
  open: boolean;
  onCancel: () => void;
  onOpenQc: (line: AsnLineRecord) => void;
  onOpenReceive: (line: AsnLineRecord) => void;
}) {
  return (
    <Modal footer={null} getContainer={false} onCancel={onCancel} open={open} title={detail?.asnNo ?? '到货通知详情'} width={980}>
      <ProCard loading={loading} bordered={false}>
        {detail ? (
          <Space direction="vertical" style={{ width: '100%' }}>
            <Descriptions column={3} size="small">
              <Descriptions.Item label="ASN单号">{detail.asnNo}</Descriptions.Item>
              <Descriptions.Item label="采购单号">{detail.procurementOrderNo || '-'}</Descriptions.Item>
              <Descriptions.Item label="供应商">{detail.supplierName || '-'}</Descriptions.Item>
              <Descriptions.Item label="预计到货">{detail.expectedArrivalDate || '-'}</Descriptions.Item>
              <Descriptions.Item label="实际到货">{detail.actualArrivalDate || '-'}</Descriptions.Item>
              <Descriptions.Item label="状态">{detail.statusLabel || detail.status || '-'}</Descriptions.Item>
            </Descriptions>
            <Table<AsnLineRecord>
              rowKey="id"
              size="small"
              pagination={false}
              columns={buildLineColumns(actions, onOpenReceive, onOpenQc)}
              dataSource={detail.lines ?? []}
            />
          </Space>
        ) : null}
      </ProCard>
    </Modal>
  );
}

function buildLineColumns(
  actions: { canReceive: boolean; canQc: boolean },
  onOpenReceive: (line: AsnLineRecord) => void,
  onOpenQc: (line: AsnLineRecord) => void,
): ColumnsType<AsnLineRecord> {
  return [
    { title: '物料编码', dataIndex: 'materialCode', key: 'materialCode', width: 120, render: (value) => value || '-' },
    { title: '物料名称', dataIndex: 'materialName', key: 'materialName', render: (value) => value || '-' },
    { title: '批次号', dataIndex: 'batchNo', key: 'batchNo', width: 130, render: (value) => value || '-' },
    { title: '预计数量', dataIndex: 'expectedQuantity', key: 'expectedQuantity', width: 100, render: formatQuantity },
    { title: '实收', dataIndex: 'receivedQuantity', key: 'receivedQuantity', width: 80, render: formatQuantity },
    { title: '合格', dataIndex: 'acceptedQuantity', key: 'acceptedQuantity', width: 80, render: formatQuantity },
    { title: '不合格', dataIndex: 'rejectedQuantity', key: 'rejectedQuantity', width: 90, render: formatQuantity },
    { title: '质检', dataIndex: 'qcStatusLabel', key: 'qcStatusLabel', width: 90, render: (value) => value || '-' },
    {
      title: '行操作',
      key: 'actions',
      width: 160,
      render: (_, line) => (
        <Space wrap>
          {actions.canReceive ? <Button size="small" aria-label={`收货 ${line.id}`} onClick={() => onOpenReceive(line)}>收货</Button> : null}
          {actions.canQc ? <Button size="small" aria-label={`质检 ${line.id}`} onClick={() => onOpenQc(line)}>质检</Button> : null}
        </Space>
      ),
    },
  ];
}

function ReceiveModal({
  asn,
  line,
  onClose,
  onReload,
}: {
  asn: AsnRecord | null;
  line: AsnLineRecord | null;
  onClose: () => void;
  onReload: () => Promise<void>;
}) {
  const { message } = App.useApp();
  const [form] = Form.useForm<{ receivedQuantity: number }>();

  async function handleSubmit() {
    const values = await form.validateFields().catch(() => null);
    if (!values) {
      return;
    }
    if (!asn || !line) {
      return;
    }
    await receiveAsnGoods({ asnId: asn.id, lines: [{ lineId: line.id, receivedQuantity: values.receivedQuantity }] });
    message.success('确认收货成功');
    onClose();
    await onReload();
  }

  return (
    <Modal destroyOnHidden getContainer={false} onCancel={onClose} onOk={handleSubmit} okText="确认收货" open={Boolean(line)} title="确认收货">
      <Form form={form} layout="vertical">
        <Form.Item label="实收数量" name="receivedQuantity" rules={[{ required: true, message: '请输入大于 0 的实收数量' }, { type: 'number', min: 0.000001, message: '请输入大于 0 的实收数量' }]}>
          <InputNumber style={{ width: '100%' }} precision={2} />
        </Form.Item>
      </Form>
    </Modal>
  );
}

function QcModal({
  line,
  onClose,
  onReload,
}: {
  line: AsnLineRecord | null;
  onClose: () => void;
  onReload: () => Promise<void>;
}) {
  const { message } = App.useApp();
  const [form] = Form.useForm<{ acceptedQuantity: number; rejectedQuantity: number; inspector?: string; conclusion?: string }>();

  async function handleSubmit() {
    const values = await form.validateFields().catch(() => null);
    if (!values) {
      return;
    }
    if (!line) {
      return;
    }
    await submitAsnQc({ ...values, lineId: line.id, inspector: values.inspector?.trim(), conclusion: values.conclusion?.trim() });
    message.success('提交质检成功');
    onClose();
    await onReload();
  }

  return (
    <Modal destroyOnHidden getContainer={false} onCancel={onClose} onOk={handleSubmit} okText="提交质检" open={Boolean(line)} title="提交质检">
      <Form form={form} layout="vertical">
        <Form.Item label="合格数量" name="acceptedQuantity" rules={[{ required: true, message: '请输入合格数量' }]}>
          <InputNumber style={{ width: '100%' }} min={0} precision={2} />
        </Form.Item>
        <Form.Item label="不合格数量" name="rejectedQuantity" rules={[{ required: true, message: '请输入不合格数量' }]}>
          <InputNumber style={{ width: '100%' }} min={0} precision={2} />
        </Form.Item>
        <Form.Item label="检验人" name="inspector"><Input /></Form.Item>
        <Form.Item label="结论说明" name="conclusion"><Input.TextArea rows={3} /></Form.Item>
      </Form>
    </Modal>
  );
}

function formatQuantity(value?: number | null): string {
  return typeof value === 'number' ? value.toLocaleString('zh-CN') : '0';
}

interface AsnLineFormValues {
  batchNo?: string;
  expectedQuantity?: number | null;
  materialId?: string;
  procurementLineId?: string;
  remark?: string;
}

interface AsnFormValues {
  expectedArrivalDate?: string;
  lines?: AsnLineFormValues[];
  procurementOrderId?: string;
  remark?: string;
  supplierId?: string;
}

function createEmptyAsnLineForm(): AsnLineFormValues {
  return {};
}

function toAsnPayload(values: AsnFormValues): CreateAsnPayload {
  return {
    procurementOrderId: trimOptional(values.procurementOrderId) ?? '',
    supplierId: trimOptional(values.supplierId) ?? '',
    expectedArrivalDate: trimOptional(values.expectedArrivalDate),
    remark: trimOptional(values.remark),
    lines: (values.lines ?? []).map((line) => ({
      procurementLineId: trimOptional(line.procurementLineId) ?? '',
      materialId: trimOptional(line.materialId) ?? '',
      expectedQuantity: Number(line.expectedQuantity ?? 0),
      batchNo: trimOptional(line.batchNo),
      remark: trimOptional(line.remark),
    })),
  };
}

async function validateAsnLines(_: unknown, value?: AsnLineFormValues[]) {
  if (!value || value.length === 0) {
    throw new Error('至少需要一行ASN明细');
  }
}

function trimOptional(value?: string | null): string | undefined {
  const nextValue = value?.trim();
  return nextValue ? nextValue : undefined;
}

function isFormValidationError(error: unknown): boolean {
  return typeof error === 'object' && error !== null && 'errorFields' in error;
}
