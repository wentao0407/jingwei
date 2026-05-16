import { CheckOutlined, DeleteOutlined, EditOutlined, EyeOutlined, PlusOutlined, ReloadOutlined, SearchOutlined } from '@ant-design/icons';
import { ProCard } from '@ant-design/pro-components';
import { App, Button, Descriptions, Form, Input, InputNumber, Modal, Select, Space, Table, Tabs, Tag } from 'antd';
import type { ColumnsType, TablePaginationConfig } from 'antd/es/table';
import { useCallback, useEffect, useState } from 'react';
import { EmptyState, ErrorState, LoadingState } from '@/components/state';
import { getCurrentUserPermissions } from '@/services/auth/authService';
import { getApiErrorMessage } from '@/services/http/apiClient';
import type { PageResult } from '@/services/master/customerService';
import {
  approveBom,
  calculateMrp,
  createBom,
  deleteBom,
  getBomDetail,
  pageBoms,
  pageMrpResults,
  updateBom,
  type BomItemRecord,
  type BomRecord,
  type MrpResultRecord,
  type SaveBomPayload,
} from '@/services/procurement/procurementService';
import { getAuthSession, setAuthSession } from '@/shared/storage/authSessionStorage';

const DEFAULT_PAGE_SIZE = 10;
const INITIAL_PAGE = 1;

const bomStatusOptions = [
  { label: '全部状态', value: '' },
  { label: '草稿', value: 'DRAFT' },
  { label: '已生效', value: 'APPROVED' },
  { label: '已作废', value: 'OBSOLETE' },
];

const mrpStatusOptions = [
  { label: '全部状态', value: '' },
  { label: '待审核', value: 'PENDING' },
  { label: '已审核', value: 'APPROVED' },
  { label: '已转采购', value: 'CONVERTED' },
  { label: '已忽略', value: 'IGNORED' },
  { label: '已过期', value: 'EXPIRED' },
];

export function BomMrpPage() {
  const { message } = App.useApp();
  const [permissions, setPermissions] = useState<string[]>(() => getAuthSession()?.permissions ?? []);
  const actions = {
    canApproveBom: permissions.includes('procurement:bom:approve'),
    canCreateBom: permissions.includes('procurement:bom:create'),
    canDeleteBom: permissions.includes('procurement:bom:delete'),
    canUpdateBom: permissions.includes('procurement:bom:update'),
    canCalculateMrp: permissions.includes('procurement:mrp:calculate'),
  };

  useEffect(() => {
    void refreshPermissions(setPermissions);
  }, []);

  return (
    <div className="system-page bom-mrp-page">
      <section className="system-page-topbar">
        <div>
          <h1>BOM 与 MRP</h1>
          <p>维护生产物料清单，查看 MRP 物料需求和采购建议。</p>
        </div>
      </section>
      <ProCard className="system-page-card" bordered={false}>
        <Tabs
          items={[
            { key: 'bom', label: 'BOM清单', children: <BomPanel actions={actions} messageApi={message} /> },
            { key: 'mrp', label: 'MRP结果', children: <MrpPanel actions={actions} messageApi={message} /> },
          ]}
        />
      </ProCard>
    </div>
  );
}

function BomPanel({
  actions,
  messageApi,
}: {
  actions: { canApproveBom: boolean; canCreateBom: boolean; canDeleteBom: boolean; canUpdateBom: boolean };
  messageApi: ReturnType<typeof App.useApp>['message'];
}) {
  const [form] = Form.useForm<BomFormValues>();
  const [spuInput, setSpuInput] = useState('');
  const [spuId, setSpuId] = useState('');
  const [status, setStatus] = useState('');
  const [currentPage, setCurrentPage] = useState(INITIAL_PAGE);
  const [pageSize, setPageSize] = useState(DEFAULT_PAGE_SIZE);
  const [loading, setLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState('');
  const [pageResult, setPageResult] = useState<PageResult<BomRecord> | null>(null);
  const [detail, setDetail] = useState<BomRecord | null>(null);
  const [detailOpen, setDetailOpen] = useState(false);
  const [formOpen, setFormOpen] = useState(false);
  const [formMode, setFormMode] = useState<'create' | 'edit'>('create');
  const [editingBomId, setEditingBomId] = useState('');
  const [deleteTarget, setDeleteTarget] = useState<BomRecord | null>(null);
  const [saving, setSaving] = useState(false);

  const loadBoms = useCallback(async () => {
    setLoading(true);
    setErrorMessage('');
    try {
      setPageResult(await pageBoms({
        current: currentPage,
        size: pageSize,
        ...(spuId ? { spuId } : {}),
        ...(status ? { status } : {}),
      }));
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error));
    } finally {
      setLoading(false);
    }
  }, [currentPage, pageSize, spuId, status]);

  useEffect(() => {
    void loadBoms();
  }, [loadBoms]);

  async function openDetail(record: BomRecord) {
    try {
      setDetail(await getBomDetail(record.id));
      setDetailOpen(true);
    } catch (error) {
      messageApi.error(getApiErrorMessage(error));
    }
  }

  async function handleApprove(record: BomRecord) {
    try {
      await approveBom(record.id);
      messageApi.success('审批BOM成功');
      await loadBoms();
    } catch (error) {
      messageApi.error(getApiErrorMessage(error));
    }
  }

  function openCreateForm() {
    setFormMode('create');
    setEditingBomId('');
    form.setFieldsValue({ items: [createEmptyBomItemForm()] });
    setFormOpen(true);
  }

  async function openEditForm(record: BomRecord) {
    try {
      const nextDetail = await getBomDetail(record.id);
      setFormMode('edit');
      setEditingBomId(record.id);
      form.setFieldsValue(toBomFormValues(nextDetail));
      setFormOpen(true);
    } catch (error) {
      messageApi.error(getApiErrorMessage(error));
    }
  }

  async function handleSaveBom() {
    try {
      const values = await form.validateFields();
      const payload = toBomPayload(values, formMode);
      setSaving(true);
      if (formMode === 'create') {
        await createBom(payload);
        messageApi.success('新增BOM成功');
      } else {
        await updateBom(editingBomId, payload);
        messageApi.success('编辑BOM成功');
      }
      setFormOpen(false);
      await loadBoms();
    } catch (error) {
      if (isFormValidationError(error)) {
        return;
      }
      messageApi.error(getApiErrorMessage(error));
    } finally {
      setSaving(false);
    }
  }

  async function handleDeleteBom() {
    if (!deleteTarget) {
      return;
    }
    try {
      setSaving(true);
      await deleteBom(deleteTarget.id);
      messageApi.success('删除BOM成功');
      setDeleteTarget(null);
      await loadBoms();
    } catch (error) {
      messageApi.error(getApiErrorMessage(error));
    } finally {
      setSaving(false);
    }
  }

  if (loading && !pageResult) {
    return <LoadingState message="正在加载BOM" />;
  }

  if (errorMessage && !pageResult) {
    return <ErrorState message={errorMessage} onRetry={loadBoms} />;
  }

  return (
    <Space direction="vertical" style={{ width: '100%' }}>
      <Space className="system-page-toolbar" wrap>
        <Input placeholder="款式ID" value={spuInput} onChange={(event) => setSpuInput(event.target.value)} />
        <Select aria-label="BOM状态筛选" options={bomStatusOptions} value={status} onChange={(value) => { setStatus(value); setCurrentPage(INITIAL_PAGE); }} />
        <Button icon={<SearchOutlined />} onClick={() => { setSpuId(spuInput.trim()); setCurrentPage(INITIAL_PAGE); }}>搜索BOM</Button>
        <Button icon={<ReloadOutlined />} onClick={loadBoms}>刷新</Button>
        {actions.canCreateBom ? (
          <Button aria-label="新增BOM" icon={<PlusOutlined />} type="primary" onClick={openCreateForm}>新增BOM</Button>
        ) : null}
      </Space>
      {pageResult?.records.length === 0 ? (
        <EmptyState message="暂无BOM" />
      ) : (
        <Table<BomRecord>
          rowKey="id"
          columns={buildBomColumns(actions, openDetail, handleApprove, openEditForm, setDeleteTarget)}
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
      <BomDetailModal bom={detail} open={detailOpen} onCancel={() => setDetailOpen(false)} />
      <BomFormModal
        form={form}
        mode={formMode}
        open={formOpen}
        saving={saving}
        onCancel={() => setFormOpen(false)}
        onSave={handleSaveBom}
      />
      <Modal
        cancelText="取消"
        confirmLoading={saving}
        getContainer={false}
        okText="确认删除"
        onCancel={() => setDeleteTarget(null)}
        onOk={handleDeleteBom}
        open={Boolean(deleteTarget)}
        title="删除BOM"
      >
        确认删除 {deleteTarget?.code} 吗？
      </Modal>
    </Space>
  );
}

function MrpPanel({
  actions,
  messageApi,
}: {
  actions: { canApproveBom: boolean; canCalculateMrp: boolean };
  messageApi: ReturnType<typeof App.useApp>['message'];
}) {
  const [batchInput, setBatchInput] = useState('');
  const [batchNo, setBatchNo] = useState('');
  const [status, setStatus] = useState('');
  const [productionOrderInput, setProductionOrderInput] = useState('');
  const [loading, setLoading] = useState(true);
  const [pageResult, setPageResult] = useState<PageResult<MrpResultRecord> | null>(null);
  const [errorMessage, setErrorMessage] = useState('');

  const loadResults = useCallback(async () => {
    setLoading(true);
    setErrorMessage('');
    try {
      setPageResult(await pageMrpResults({
        current: INITIAL_PAGE,
        size: DEFAULT_PAGE_SIZE,
        ...(batchNo ? { batchNo } : {}),
        ...(status ? { status } : {}),
      }));
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error));
    } finally {
      setLoading(false);
    }
  }, [batchNo, status]);

  useEffect(() => {
    void loadResults();
  }, [loadResults]);

  async function handleCalculate() {
    try {
      const productionOrderIds = productionOrderInput.split(',').map((id) => id.trim()).filter(Boolean);
      const result = await calculateMrp({ productionOrderIds });
      messageApi.success(`MRP计算完成：${result.batchNo}`);
      setBatchNo(result.batchNo);
      setBatchInput(result.batchNo);
    } catch (error) {
      messageApi.error(getApiErrorMessage(error));
    }
  }

  if (loading && !pageResult) {
    return <LoadingState message="正在加载MRP结果" />;
  }

  if (errorMessage && !pageResult) {
    return <ErrorState message={errorMessage} onRetry={loadResults} />;
  }

  return (
    <Space direction="vertical" style={{ width: '100%' }}>
      <Space className="system-page-toolbar" wrap>
        <Input placeholder="MRP批次号" value={batchInput} onChange={(event) => setBatchInput(event.target.value)} />
        <Select aria-label="MRP状态筛选" options={mrpStatusOptions} value={status} onChange={setStatus} />
        <Button icon={<SearchOutlined />} onClick={() => setBatchNo(batchInput.trim())}>搜索MRP</Button>
        {actions.canCalculateMrp ? (
          <>
            <Input placeholder="生产订单ID，多个用逗号分隔" value={productionOrderInput} onChange={(event) => setProductionOrderInput(event.target.value)} />
            <Button type="primary" onClick={handleCalculate}>执行MRP计算</Button>
          </>
        ) : null}
      </Space>
      <Table<MrpResultRecord>
        rowKey="id"
        columns={mrpColumns}
        dataSource={pageResult?.records ?? []}
        loading={loading}
        pagination={false}
      />
    </Space>
  );
}

function buildBomColumns(
  actions: { canApproveBom: boolean; canDeleteBom: boolean; canUpdateBom: boolean },
  onDetail: (record: BomRecord) => void,
  onApprove: (record: BomRecord) => void,
  onEdit: (record: BomRecord) => void,
  onDelete: (record: BomRecord) => void,
): ColumnsType<BomRecord> {
  return [
    { title: 'BOM编码', dataIndex: 'code', key: 'code', width: 150 },
    { title: '款式', key: 'spu', render: (_, record) => record.spuName || record.spuCode || '-' },
    { title: '版本', dataIndex: 'bomVersion', key: 'bomVersion', width: 80, render: (value) => value ?? '-' },
    { title: '状态', key: 'status', width: 110, render: (_, record) => <Tag>{record.statusLabel || record.status || '-'}</Tag> },
    {
      title: '操作',
      key: 'actions',
      width: 180,
      render: (_, record) => (
        <Space wrap>
          <Button icon={<EyeOutlined />} size="small" aria-label={`详情 ${record.code}`} onClick={() => onDetail(record)}>详情</Button>
          {actions.canUpdateBom && record.status === 'DRAFT' ? (
            <Button icon={<EditOutlined />} size="small" aria-label={`编辑 ${record.code}`} onClick={() => onEdit(record)}>编辑</Button>
          ) : null}
          {actions.canDeleteBom && record.status === 'DRAFT' ? (
            <Button danger icon={<DeleteOutlined />} size="small" aria-label={`删除 ${record.code}`} onClick={() => onDelete(record)}>删除</Button>
          ) : null}
          {actions.canApproveBom && record.status === 'DRAFT' ? (
            <Button icon={<CheckOutlined />} size="small" aria-label={`审批 ${record.code}`} onClick={() => onApprove(record)}>审批</Button>
          ) : null}
        </Space>
      ),
    },
  ];
}

function BomFormModal({
  form,
  mode,
  open,
  saving,
  onCancel,
  onSave,
}: {
  form: ReturnType<typeof Form.useForm<BomFormValues>>[0];
  mode: 'create' | 'edit';
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
      okText="保存BOM"
      onCancel={onCancel}
      onOk={onSave}
      open={open}
      title={mode === 'create' ? '新增BOM' : '编辑BOM'}
      width={980}
    >
      <Form<BomFormValues> form={form} layout="vertical">
        {mode === 'create' ? (
          <Form.Item label="款式ID" name="spuId" rules={[{ required: true, message: '请输入款式ID' }]}>
            <Input />
          </Form.Item>
        ) : null}
        <Space align="start" style={{ width: '100%' }} wrap>
          <Form.Item label="生效日期" name="effectiveFrom">
            <Input placeholder="YYYY-MM-DD" />
          </Form.Item>
          <Form.Item label="失效日期" name="effectiveTo">
            <Input placeholder="YYYY-MM-DD" />
          </Form.Item>
        </Space>
        <Form.Item label="BOM备注" name="remark">
          <Input.TextArea autoSize={{ minRows: 2, maxRows: 4 }} />
        </Form.Item>
        <Form.List name="items" rules={[{ validator: validateBomItems }]}>
          {(fields, { add, remove }) => (
            <Space direction="vertical" style={{ width: '100%' }}>
              {fields.map((field) => (
                <ProCard key={field.key} bordered size="small">
                  <Space align="start" wrap>
                    <Form.Item label="物料ID" name={[field.name, 'materialId']} rules={[{ required: true, message: '请输入物料ID' }]}>
                      <Input />
                    </Form.Item>
                    <Form.Item label="物料类型" name={[field.name, 'materialType']} rules={[{ required: true, message: '请输入物料类型' }]}>
                      <Input placeholder="FABRIC/TRIM/PACKAGING" />
                    </Form.Item>
                    <Form.Item label="消耗类型" name={[field.name, 'consumptionType']} rules={[{ required: true, message: '请输入消耗类型' }]}>
                      <Input placeholder="FIXED_PER_PIECE" />
                    </Form.Item>
                    <Form.Item label="基准用量" name={[field.name, 'baseConsumption']} rules={[{ required: true, message: '请输入基准用量' }]}>
                      <InputNumber min={0} />
                    </Form.Item>
                    <Form.Item label="基准尺码ID" name={[field.name, 'baseSizeId']}>
                      <Input />
                    </Form.Item>
                    <Form.Item label="单位" name={[field.name, 'unit']} rules={[{ required: true, message: '请输入单位' }]}>
                      <Input />
                    </Form.Item>
                    <Form.Item label="损耗率" name={[field.name, 'wastageRate']}>
                      <InputNumber min={0} max={1} step={0.01} />
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
              <Button onClick={() => add(createEmptyBomItemForm())}>新增BOM行</Button>
            </Space>
          )}
        </Form.List>
      </Form>
    </Modal>
  );
}

function BomDetailModal({ bom, open, onCancel }: { bom: BomRecord | null; open: boolean; onCancel: () => void }) {
  return (
    <Modal footer={null} getContainer={false} onCancel={onCancel} open={open} title={bom?.code ?? 'BOM详情'} width={900}>
      {bom ? (
        <Space direction="vertical" style={{ width: '100%' }}>
          <Descriptions column={3} size="small">
            <Descriptions.Item label="BOM编码">{bom.code}</Descriptions.Item>
            <Descriptions.Item label="款式">{bom.spuName || bom.spuCode || '-'}</Descriptions.Item>
            <Descriptions.Item label="状态">{bom.statusLabel || bom.status || '-'}</Descriptions.Item>
          </Descriptions>
          <Table<BomItemRecord> rowKey="id" size="small" pagination={false} columns={bomItemColumns} dataSource={bom.items ?? []} />
        </Space>
      ) : null}
    </Modal>
  );
}

const bomItemColumns: ColumnsType<BomItemRecord> = [
  { title: '物料编码', dataIndex: 'materialCode', key: 'materialCode', width: 120, render: (value) => value || '-' },
  { title: '物料名称', dataIndex: 'materialName', key: 'materialName', render: (value) => value || '-' },
  { title: '消耗类型', dataIndex: 'consumptionTypeLabel', key: 'consumptionTypeLabel', width: 120, render: (value) => value || '-' },
  { title: '基准用量', dataIndex: 'baseConsumption', key: 'baseConsumption', width: 100, render: formatQuantity },
  { title: '单位', dataIndex: 'unit', key: 'unit', width: 70, render: (value) => value || '-' },
  { title: '损耗率', dataIndex: 'wastageRate', key: 'wastageRate', width: 90, render: formatPercent },
];

const mrpColumns: ColumnsType<MrpResultRecord> = [
  { title: '批次号', dataIndex: 'batchNo', key: 'batchNo', width: 150, render: (value) => value || '-' },
  { title: '物料编码', dataIndex: 'materialCode', key: 'materialCode', width: 120, render: (value) => value || '-' },
  { title: '物料名称', dataIndex: 'materialName', key: 'materialName', render: (value) => value || '-' },
  { title: '毛需求', dataIndex: 'grossDemand', key: 'grossDemand', width: 90, render: formatQuantity },
  { title: '可用库存', dataIndex: 'allocatedStock', key: 'allocatedStock', width: 90, render: formatQuantity },
  { title: '在途', dataIndex: 'inTransitQuantity', key: 'inTransitQuantity', width: 80, render: formatQuantity },
  { title: '净需求', dataIndex: 'netDemand', key: 'netDemand', width: 90, render: formatQuantity },
  { title: '建议采购', dataIndex: 'suggestedQuantity', key: 'suggestedQuantity', width: 100, render: formatQuantity },
  { title: '供应商', dataIndex: 'suggestedSupplierName', key: 'suggestedSupplierName', width: 120, render: (value) => value || '-' },
  { title: '预估成本', dataIndex: 'estimatedCost', key: 'estimatedCost', width: 100, render: formatAmount },
  { title: '状态', dataIndex: 'statusLabel', key: 'statusLabel', width: 90, render: (value) => value || '-' },
];

interface BomItemFormValues {
  baseConsumption?: number | null;
  baseSizeId?: string;
  consumptionType?: string;
  materialId?: string;
  materialType?: string;
  remark?: string;
  unit?: string;
  wastageRate?: number | null;
}

interface BomFormValues {
  effectiveFrom?: string;
  effectiveTo?: string;
  items?: BomItemFormValues[];
  remark?: string;
  spuId?: string;
}

function createEmptyBomItemForm(): BomItemFormValues {
  return {
    consumptionType: 'FIXED_PER_PIECE',
    materialType: 'FABRIC',
    unit: '米',
  };
}

function toBomFormValues(bom: BomRecord): BomFormValues {
  return {
    effectiveFrom: bom.effectiveFrom ?? '',
    effectiveTo: bom.effectiveTo ?? '',
    items: (bom.items?.length ? bom.items : [createEmptyBomItemForm()]).map((item) => ({
      baseConsumption: item.baseConsumption ?? undefined,
      consumptionType: item.consumptionType ?? '',
      materialId: item.materialId ?? '',
      materialType: item.materialType ?? '',
      remark: item.remark ?? '',
      unit: item.unit ?? '',
      wastageRate: item.wastageRate ?? undefined,
    })),
    remark: bom.remark ?? '',
    spuId: bom.spuId ?? '',
  };
}

function toBomPayload(values: BomFormValues, mode: 'create' | 'edit'): SaveBomPayload {
  return {
    ...(mode === 'create' ? { spuId: trimOptional(values.spuId) } : {}),
    effectiveFrom: trimOptional(values.effectiveFrom),
    effectiveTo: trimOptional(values.effectiveTo),
    remark: trimOptional(values.remark),
    items: (values.items ?? []).map((item) => ({
      materialId: trimOptional(item.materialId) ?? '',
      materialType: trimOptional(item.materialType) ?? '',
      consumptionType: trimOptional(item.consumptionType) ?? '',
      baseConsumption: Number(item.baseConsumption ?? 0),
      baseSizeId: trimOptional(item.baseSizeId),
      unit: trimOptional(item.unit) ?? '',
      wastageRate: typeof item.wastageRate === 'number' ? item.wastageRate : undefined,
      remark: trimOptional(item.remark),
    })),
  };
}

async function validateBomItems(_: unknown, value?: BomItemFormValues[]) {
  if (!value || value.length === 0) {
    throw new Error('至少需要一行BOM明细');
  }
}

function trimOptional(value?: string | null): string | undefined {
  const nextValue = value?.trim();
  return nextValue ? nextValue : undefined;
}

function isFormValidationError(error: unknown): boolean {
  return typeof error === 'object' && error !== null && 'errorFields' in error;
}

async function refreshPermissions(setPermissions: (permissions: string[]) => void) {
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

function formatAmount(value?: number | null): string {
  return typeof value === 'number'
    ? value.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
    : '0.00';
}

function formatQuantity(value?: number | null): string {
  return typeof value === 'number' ? value.toLocaleString('zh-CN') : '0';
}

function formatPercent(value?: number | null): string {
  return typeof value === 'number' ? `${Math.round(value * 100)}%` : '-';
}
