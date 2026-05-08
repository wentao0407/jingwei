import { PlusOutlined, ReloadOutlined, SearchOutlined } from '@ant-design/icons';
import { ProCard } from '@ant-design/pro-components';
import { App, Button, Form, Input, InputNumber, Modal, Popconfirm, Select, Space, Table, Tag } from 'antd';
import type { ColumnsType, TablePaginationConfig } from 'antd/es/table';
import { useCallback, useEffect, useState } from 'react';
import { EmptyState, ErrorState, LoadingState } from '@/components/state';
import { getCurrentUserPermissions } from '@/services/auth/authService';
import { getApiErrorMessage } from '@/services/http/apiClient';
import {
  activateSupplier,
  createSupplier,
  deactivateSupplier,
  deleteSupplier,
  listSuppliers,
  updateSupplier,
  type CreateSupplierPayload,
  type SupplierRecord,
  type SupplierQueryParams,
  type UpdateSupplierPayload,
} from '@/services/master/supplierService';
import type { PageResult } from '@/services/master/customerService';
import { getAuthSession, setAuthSession } from '@/shared/storage/authSessionStorage';

const DEFAULT_PAGE_SIZE = 10;
const INITIAL_PAGE = 1;
const NAME_MAX_LENGTH = 128;
const SHORT_NAME_MAX_LENGTH = 64;
const CONTACT_PERSON_MAX_LENGTH = 32;
const PHONE_MAX_LENGTH = 20;
const phonePattern = /^1[3-9]\d{9}$/;

const supplierTypeOptions = [
  { label: '全部类型', value: '' },
  { label: '面料供应商', value: 'FABRIC' },
  { label: '辅料供应商', value: 'TRIM' },
  { label: '包装供应商', value: 'PACKAGING' },
  { label: '综合供应商', value: 'COMPOSITE' },
];

const qualificationOptions = [
  { label: '全部资质', value: '' },
  { label: '合格', value: 'QUALIFIED' },
  { label: '待审核', value: 'PENDING' },
  { label: '不合格', value: 'DISQUALIFIED' },
];

const statusOptions = [
  { label: '全部状态', value: '' },
  { label: '启用', value: 'ACTIVE' },
  { label: '停用', value: 'INACTIVE' },
];

const settlementTypeOptions = [
  { label: '月结', value: 'MONTHLY' },
  { label: '季结', value: 'QUARTERLY' },
  { label: '货到付款', value: 'COD' },
];

type SupplierFormValues = CreateSupplierPayload & UpdateSupplierPayload;
type FormMode = 'create' | 'edit';

export function SupplierManagementPage() {
  const { message } = App.useApp();
  const [form] = Form.useForm<SupplierFormValues>();
  const [keywordInput, setKeywordInput] = useState('');
  const [keyword, setKeyword] = useState('');
  const [type, setType] = useState('');
  const [qualificationStatus, setQualificationStatus] = useState('');
  const [status, setStatus] = useState('');
  const [currentPage, setCurrentPage] = useState(INITIAL_PAGE);
  const [pageSize, setPageSize] = useState(DEFAULT_PAGE_SIZE);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');
  const [pageResult, setPageResult] = useState<PageResult<SupplierRecord> | null>(null);
  const [formOpen, setFormOpen] = useState(false);
  const [formMode, setFormMode] = useState<FormMode>('create');
  const [editingSupplier, setEditingSupplier] = useState<SupplierRecord | null>(null);
  const [permissions, setPermissions] = useState<string[]>(() => getAuthSession()?.permissions ?? []);
  const actions = getSupplierActions(permissions);

  const loadSuppliers = useCallback(async () => {
    setLoading(true);
    setErrorMessage('');
    try {
      const query: SupplierQueryParams = {
        current: currentPage,
        size: pageSize,
        ...(keyword ? { keyword } : {}),
        ...(type ? { type } : {}),
        ...(qualificationStatus ? { qualificationStatus } : {}),
        ...(status ? { status } : {}),
      };
      setPageResult(await listSuppliers(query));
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error));
    } finally {
      setLoading(false);
    }
  }, [currentPage, pageSize, keyword, type, qualificationStatus, status]);

  useEffect(() => {
    void loadSuppliers();
  }, [loadSuppliers]);

  useEffect(() => {
    void refreshPermissions();
  }, []);

  const handleSearch = () => {
    setKeyword(keywordInput.trim());
    setCurrentPage(INITIAL_PAGE);
  };

  const handleTableChange = (pagination: TablePaginationConfig) => {
    setCurrentPage(pagination.current ?? INITIAL_PAGE);
    setPageSize(pagination.pageSize ?? DEFAULT_PAGE_SIZE);
  };

  const openCreateForm = () => {
    setFormMode('create');
    setEditingSupplier(null);
    form.resetFields();
    setFormOpen(true);
  };

  function openEditForm(supplier: SupplierRecord) {
    setFormMode('edit');
    setEditingSupplier(supplier);
    form.setFieldsValue({
      name: supplier.name,
      shortName: supplier.shortName ?? '',
      contactPerson: supplier.contactPerson ?? '',
      contactPhone: supplier.contactPhone ?? '',
      address: supplier.address ?? '',
      settlementType: supplier.settlementType ?? undefined,
      leadTimeDays: supplier.leadTimeDays ?? undefined,
      qualificationStatus: supplier.qualificationStatus ?? 'PENDING',
      remark: supplier.remark ?? '',
    });
    setFormOpen(true);
  }

  const handleSave = async () => {
    try {
      const values = await form.validateFields();
      setSaving(true);
      if (formMode === 'create') {
        await createSupplier(toCreatePayload(values));
        message.success('供应商创建成功');
      } else if (editingSupplier) {
        await updateSupplier(editingSupplier.id, toUpdatePayload(values));
        message.success('供应商更新成功');
      }
      setFormOpen(false);
      await loadSuppliers();
    } catch (error) {
      if (!isFormValidationError(error)) {
        message.error(getApiErrorMessage(error));
      }
    } finally {
      setSaving(false);
    }
  };

  async function handleActivate(supplier: SupplierRecord) {
    await runAction(() => activateSupplier(supplier.id), '供应商已启用');
  }

  async function handleDeactivate(supplier: SupplierRecord) {
    await runAction(() => deactivateSupplier(supplier.id), '供应商已停用');
  }

  async function handleDelete(supplier: SupplierRecord) {
    await runAction(() => deleteSupplier(supplier.id), '供应商已删除');
  }

  async function runAction(action: () => Promise<void>, successMessage: string) {
    try {
      await action();
      message.success(successMessage);
      await loadSuppliers();
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
    return <LoadingState message="正在加载供应商数据" />;
  }

  if (errorMessage && !pageResult) {
    return <ErrorState message={errorMessage} onRetry={loadSuppliers} />;
  }

  return (
    <div className="system-page supplier-management-page">
      <section className="system-page-topbar">
        <div>
          <h1>供应商管理</h1>
          <p>维护供应商档案、资质状态、交货周期和启停状态。</p>
        </div>
        {actions.canCreate ? (
          <Button type="primary" icon={<PlusOutlined />} aria-label="新建供应商" onClick={openCreateForm}>
            新建供应商
          </Button>
        ) : null}
      </section>

      <ProCard className="system-filter-card" bordered>
        <Space wrap>
          <Input allowClear placeholder="搜索供应商编码/名称" value={keywordInput} onChange={(event) => setKeywordInput(event.target.value)} onPressEnter={handleSearch} />
          <Select aria-label="供应商类型" value={type} options={supplierTypeOptions} onChange={(value) => { setType(value); setCurrentPage(INITIAL_PAGE); }} />
          <Select aria-label="资质状态" value={qualificationStatus} options={qualificationOptions} onChange={(value) => { setQualificationStatus(value); setCurrentPage(INITIAL_PAGE); }} />
          <Select aria-label="状态" value={status} options={statusOptions} onChange={(value) => { setStatus(value); setCurrentPage(INITIAL_PAGE); }} />
          <Button type="primary" icon={<SearchOutlined />} onClick={handleSearch}>查询</Button>
          <Button icon={<ReloadOutlined />} onClick={loadSuppliers}>刷新</Button>
        </Space>
      </ProCard>

      {errorMessage ? <ErrorState message={errorMessage} onRetry={loadSuppliers} /> : null}

      <ProCard className="system-table-card" bordered>
        <Table<SupplierRecord>
          rowKey="id"
          columns={buildColumns({ actions, onActivate: handleActivate, onDeactivate: handleDeactivate, onDelete: handleDelete, onEdit: openEditForm })}
          dataSource={pageResult?.records ?? []}
          loading={loading}
          locale={{ emptyText: <EmptyState message="暂无供应商数据" /> }}
          pagination={{ current: currentPage, pageSize, total: pageResult?.total ?? 0, showSizeChanger: true }}
          onChange={handleTableChange}
        />
      </ProCard>

      <Modal title={formMode === 'create' ? '新建供应商' : '编辑供应商'} open={formOpen} confirmLoading={saving} okText="保存" cancelText="取消" okButtonProps={{ 'aria-label': '保存' }} onCancel={() => setFormOpen(false)} onOk={handleSave} destroyOnHidden forceRender>
        <Form<SupplierFormValues> form={form} layout="vertical" preserve={false}>
          <Form.Item label="供应商名称" name="name" normalize={normalizeTextInput} rules={[{ required: true, whitespace: true, message: '请输入供应商名称' }, { max: NAME_MAX_LENGTH, message: `供应商名称最长${NAME_MAX_LENGTH}个字符` }]}>
            <Input allowClear maxLength={NAME_MAX_LENGTH} />
          </Form.Item>
          <Form.Item label="简称" name="shortName" normalize={normalizeTextInput} rules={[{ max: SHORT_NAME_MAX_LENGTH, message: `简称最长${SHORT_NAME_MAX_LENGTH}个字符` }]}>
            <Input allowClear maxLength={SHORT_NAME_MAX_LENGTH} />
          </Form.Item>
          {formMode === 'create' ? (
            <Form.Item label="供应商类型" name="type" rules={[{ required: true, message: '请选择供应商类型' }]}>
              <Select options={supplierTypeOptions.slice(1)} />
            </Form.Item>
          ) : null}
          {formMode === 'edit' ? (
            <Form.Item label="资质状态" name="qualificationStatus">
              <Select options={qualificationOptions.slice(1)} />
            </Form.Item>
          ) : null}
          <Form.Item label="联系人" name="contactPerson" normalize={normalizeTextInput} rules={[{ max: CONTACT_PERSON_MAX_LENGTH, message: `联系人最长${CONTACT_PERSON_MAX_LENGTH}个字符` }]}>
            <Input allowClear maxLength={CONTACT_PERSON_MAX_LENGTH} />
          </Form.Item>
          <Form.Item label="联系电话" name="contactPhone" normalize={normalizeTextInput} rules={[{ max: PHONE_MAX_LENGTH, message: `联系电话最长${PHONE_MAX_LENGTH}个字符` }, { pattern: phonePattern, message: '请输入正确的手机号' }]}>
            <Input allowClear maxLength={PHONE_MAX_LENGTH} />
          </Form.Item>
          <Form.Item label="地址" name="address" normalize={normalizeTextInput}>
            <Input allowClear />
          </Form.Item>
          <Form.Item label="结算方式" name="settlementType">
            <Select allowClear options={settlementTypeOptions} />
          </Form.Item>
          <Form.Item label="平均交货天数" name="leadTimeDays">
            <InputNumber className="system-number-input" min={0} precision={0} />
          </Form.Item>
          <Form.Item label="备注" name="remark" normalize={normalizeTextInput}>
            <Input.TextArea rows={2} allowClear />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}

interface SupplierActionPermissions {
  canActivate: boolean;
  canCreate: boolean;
  canDeactivate: boolean;
  canDelete: boolean;
  canUpdate: boolean;
}

function buildColumns(handlers: { actions: SupplierActionPermissions; onActivate: (supplier: SupplierRecord) => void; onDeactivate: (supplier: SupplierRecord) => void; onDelete: (supplier: SupplierRecord) => void; onEdit: (supplier: SupplierRecord) => void }): ColumnsType<SupplierRecord> {
  const columns: ColumnsType<SupplierRecord> = [
    { title: '供应商编码', dataIndex: 'code' },
    { title: '供应商名称', dataIndex: 'name' },
    { title: '类型', dataIndex: 'type', render: (value: unknown) => <Tag>{supplierTypeLabel(value)}</Tag> },
    { title: '资质', dataIndex: 'qualificationStatus', render: (value: unknown) => <QualificationTag status={String(value || 'PENDING')} /> },
    { title: '交货天数', dataIndex: 'leadTimeDays', render: (value: unknown) => value ?? '-' },
    { title: '联系电话', dataIndex: 'contactPhone', render: (value: unknown) => value || '-' },
    { title: '状态', dataIndex: 'status', render: (value: unknown) => <StatusTag status={String(value)} /> },
    {
      title: '操作',
      dataIndex: 'actions',
      render: (_: unknown, supplier: SupplierRecord) => <ActionButtons record={supplier} actions={handlers.actions} handlers={handlers} />,
    },
  ];
  return columns.filter((column) => column.title !== '操作' || hasAnyAction(handlers.actions));
}

function ActionButtons({ record, actions, handlers }: { record: SupplierRecord; actions: SupplierActionPermissions; handlers: { onActivate: (record: SupplierRecord) => void; onDeactivate: (record: SupplierRecord) => void; onDelete: (record: SupplierRecord) => void; onEdit: (record: SupplierRecord) => void } }) {
  return (
    <Space>
      {actions.canUpdate ? <Button type="link" aria-label={`编辑 ${record.name}`} onClick={() => handlers.onEdit(record)}>编辑</Button> : null}
      {record.status === 'ACTIVE' && actions.canDeactivate ? (
        <Popconfirm title="确认停用该供应商？" okText="确认停用" cancelText="取消" onConfirm={() => handlers.onDeactivate(record)}>
          <Button type="link" aria-label={`停用 ${record.name}`}>停用</Button>
        </Popconfirm>
      ) : null}
      {record.status === 'INACTIVE' && actions.canActivate ? (
        <Popconfirm title="确认启用该供应商？" okText="确认启用" cancelText="取消" onConfirm={() => handlers.onActivate(record)}>
          <Button type="link" aria-label={`启用 ${record.name}`}>启用</Button>
        </Popconfirm>
      ) : null}
      {actions.canDelete ? (
        <Popconfirm title="确认删除该供应商？" description="已被采购订单引用时后端会拒绝删除。" okText="确认删除" cancelText="取消" onConfirm={() => handlers.onDelete(record)}>
          <Button danger type="link" aria-label={`删除 ${record.name}`}>删除</Button>
        </Popconfirm>
      ) : null}
    </Space>
  );
}

function getSupplierActions(permissions: string[]): SupplierActionPermissions {
  return {
    canCreate: permissions.includes('master:supplier:create'),
    canUpdate: permissions.includes('master:supplier:update'),
    canActivate: permissions.includes('master:supplier:activate'),
    canDeactivate: permissions.includes('master:supplier:deactivate'),
    canDelete: permissions.includes('master:supplier:delete'),
  };
}

function hasAnyAction(actions: SupplierActionPermissions): boolean {
  return actions.canUpdate || actions.canActivate || actions.canDeactivate || actions.canDelete;
}

function supplierTypeLabel(value: unknown): string {
  return supplierTypeOptions.find((option) => option.value === value)?.label ?? String(value || '-');
}

function QualificationTag({ status }: { status: string }) {
  const label = qualificationOptions.find((option) => option.value === status)?.label ?? status;
  const color = status === 'QUALIFIED' ? 'green' : status === 'DISQUALIFIED' ? 'red' : 'gold';
  return <Tag color={color}>{label}</Tag>;
}

function StatusTag({ status }: { status: string }) {
  return status === 'ACTIVE' ? <Tag color="green">启用</Tag> : <Tag color="default">停用</Tag>;
}

function toCreatePayload(values: SupplierFormValues): CreateSupplierPayload {
  return removeEmptyFields(values) as CreateSupplierPayload;
}

function toUpdatePayload(values: SupplierFormValues): UpdateSupplierPayload {
  const payload: Partial<SupplierFormValues> = { ...values };
  delete payload.type;
  return removeEmptyFields(payload) as UpdateSupplierPayload;
}

function removeEmptyFields<T extends object>(payload: T): Partial<T> {
  return Object.fromEntries(Object.entries(payload).filter(([, value]) => value !== undefined && value !== null && value !== '')) as Partial<T>;
}

function isFormValidationError(error: unknown): boolean {
  return typeof error === 'object' && error !== null && 'errorFields' in error;
}

function normalizeTextInput(value: unknown): unknown {
  return typeof value === 'string' ? value.trim() : value;
}
