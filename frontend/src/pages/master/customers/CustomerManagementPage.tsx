import { PlusOutlined, ReloadOutlined, SearchOutlined } from '@ant-design/icons';
import { ProCard } from '@ant-design/pro-components';
import { App, Button, Form, Input, InputNumber, Modal, Popconfirm, Select, Space, Table, Tag } from 'antd';
import type { ColumnsType, TablePaginationConfig } from 'antd/es/table';
import { useCallback, useEffect, useState } from 'react';
import { EmptyState, ErrorState, LoadingState } from '@/components/state';
import { getCurrentUserPermissions } from '@/services/auth/authService';
import { getApiErrorMessage } from '@/services/http/apiClient';
import {
  activateCustomer,
  createCustomer,
  deactivateCustomer,
  deleteCustomer,
  listCustomers,
  updateCustomer,
  type CreateCustomerPayload,
  type CustomerRecord,
  type PageResult,
  type UpdateCustomerPayload,
} from '@/services/master/customerService';
import { getAuthSession, setAuthSession } from '@/shared/storage/authSessionStorage';

const DEFAULT_PAGE_SIZE = 10;
const INITIAL_PAGE = 1;
const NAME_MAX_LENGTH = 128;
const SHORT_NAME_MAX_LENGTH = 64;
const CONTACT_PERSON_MAX_LENGTH = 32;
const PHONE_MAX_LENGTH = 20;
const phonePattern = /^1[3-9]\d{9}$/;

const customerTypeOptions = [
  { label: '全部类型', value: '' },
  { label: '批发客户', value: 'WHOLESALE' },
  { label: '零售客户', value: 'RETAIL' },
  { label: '线上客户', value: 'ONLINE' },
  { label: '加盟客户', value: 'FRANCHISE' },
];

const levelOptions = [
  { label: '全部等级', value: '' },
  { label: 'A级', value: 'A' },
  { label: 'B级', value: 'B' },
  { label: 'C级', value: 'C' },
  { label: 'D级', value: 'D' },
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

type CustomerFormValues = CreateCustomerPayload & UpdateCustomerPayload;
type FormMode = 'create' | 'edit';

export function CustomerManagementPage() {
  const { message } = App.useApp();
  const [form] = Form.useForm<CustomerFormValues>();
  const [keywordInput, setKeywordInput] = useState('');
  const [keyword, setKeyword] = useState('');
  const [type, setType] = useState('');
  const [level, setLevel] = useState('');
  const [status, setStatus] = useState('');
  const [currentPage, setCurrentPage] = useState(INITIAL_PAGE);
  const [pageSize, setPageSize] = useState(DEFAULT_PAGE_SIZE);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');
  const [pageResult, setPageResult] = useState<PageResult<CustomerRecord> | null>(null);
  const [formOpen, setFormOpen] = useState(false);
  const [formMode, setFormMode] = useState<FormMode>('create');
  const [editingCustomer, setEditingCustomer] = useState<CustomerRecord | null>(null);
  const [permissions, setPermissions] = useState<string[]>(() => getAuthSession()?.permissions ?? []);
  const actions = getCustomerActions(permissions);

  const loadCustomers = useCallback(async () => {
    setLoading(true);
    setErrorMessage('');
    try {
      setPageResult(
        await listCustomers({
          current: currentPage,
          size: pageSize,
          ...(keyword ? { keyword } : {}),
          ...(type ? { type } : {}),
          ...(level ? { level } : {}),
          ...(status ? { status } : {}),
        }),
      );
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error));
    } finally {
      setLoading(false);
    }
  }, [currentPage, pageSize, keyword, type, level, status]);

  useEffect(() => {
    void loadCustomers();
  }, [loadCustomers]);

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
    setEditingCustomer(null);
    form.resetFields();
    setFormOpen(true);
  };

  function openEditForm(customer: CustomerRecord) {
    setFormMode('edit');
    setEditingCustomer(customer);
    form.setFieldsValue({
      name: customer.name,
      shortName: customer.shortName ?? '',
      level: customer.level ?? 'C',
      contactPerson: customer.contactPerson ?? '',
      contactPhone: customer.contactPhone ?? '',
      address: customer.address ?? '',
      deliveryAddress: customer.deliveryAddress ?? '',
      settlementType: customer.settlementType ?? undefined,
      creditLimit: customer.creditLimit ?? undefined,
      remark: customer.remark ?? '',
    });
    setFormOpen(true);
  }

  const handleSave = async () => {
    try {
      const values = await form.validateFields();
      setSaving(true);
      if (formMode === 'create') {
        await createCustomer(toCreatePayload(values));
        message.success('客户创建成功');
      } else if (editingCustomer) {
        await updateCustomer(editingCustomer.id, toUpdatePayload(values));
        message.success('客户更新成功');
      }
      setFormOpen(false);
      await loadCustomers();
    } catch (error) {
      if (!isFormValidationError(error)) {
        message.error(getApiErrorMessage(error));
      }
    } finally {
      setSaving(false);
    }
  };

  async function handleActivate(customer: CustomerRecord) {
    await runAction(() => activateCustomer(customer.id), '客户已启用');
  }

  async function handleDeactivate(customer: CustomerRecord) {
    await runAction(() => deactivateCustomer(customer.id), '客户已停用');
  }

  async function handleDelete(customer: CustomerRecord) {
    await runAction(() => deleteCustomer(customer.id), '客户已删除');
  }

  async function runAction(action: () => Promise<void>, successMessage: string) {
    try {
      await action();
      message.success(successMessage);
      await loadCustomers();
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
    return <LoadingState message="正在加载客户数据" />;
  }

  if (errorMessage && !pageResult) {
    return <ErrorState message={errorMessage} onRetry={loadCustomers} />;
  }

  return (
    <div className="system-page customer-management-page">
      <section className="system-page-topbar">
        <div>
          <h1>客户管理</h1>
          <p>维护客户档案、等级、结算方式和启停状态。</p>
        </div>
        {actions.canCreate ? (
          <Button type="primary" icon={<PlusOutlined />} aria-label="新建客户" onClick={openCreateForm}>
            新建客户
          </Button>
        ) : null}
      </section>

      <ProCard className="system-filter-card" bordered>
        <Space wrap>
          <Input
            allowClear
            placeholder="搜索客户编码/名称"
            value={keywordInput}
            onChange={(event) => setKeywordInput(event.target.value)}
            onPressEnter={handleSearch}
          />
          <Select aria-label="客户类型" value={type} options={customerTypeOptions} onChange={(value) => { setType(value); setCurrentPage(INITIAL_PAGE); }} />
          <Select aria-label="客户等级" value={level} options={levelOptions} onChange={(value) => { setLevel(value); setCurrentPage(INITIAL_PAGE); }} />
          <Select aria-label="状态" value={status} options={statusOptions} onChange={(value) => { setStatus(value); setCurrentPage(INITIAL_PAGE); }} />
          <Button type="primary" icon={<SearchOutlined />} onClick={handleSearch}>
            查询
          </Button>
          <Button icon={<ReloadOutlined />} onClick={loadCustomers}>
            刷新
          </Button>
        </Space>
      </ProCard>

      {errorMessage ? <ErrorState message={errorMessage} onRetry={loadCustomers} /> : null}

      <ProCard className="system-table-card" bordered>
        <Table<CustomerRecord>
          rowKey="id"
          columns={buildColumns({ actions, onActivate: handleActivate, onDeactivate: handleDeactivate, onDelete: handleDelete, onEdit: openEditForm })}
          dataSource={pageResult?.records ?? []}
          loading={loading}
          locale={{ emptyText: <EmptyState message="暂无客户数据" /> }}
          pagination={{ current: currentPage, pageSize, total: pageResult?.total ?? 0, showSizeChanger: true }}
          onChange={handleTableChange}
        />
      </ProCard>

      <Modal title={formMode === 'create' ? '新建客户' : '编辑客户'} open={formOpen} confirmLoading={saving} okText="保存" cancelText="取消" okButtonProps={{ 'aria-label': '保存' }} onCancel={() => setFormOpen(false)} onOk={handleSave} destroyOnHidden forceRender>
        <Form<CustomerFormValues> form={form} layout="vertical" preserve={false}>
          <Form.Item label="客户名称" name="name" normalize={normalizeTextInput} rules={[{ required: true, whitespace: true, message: '请输入客户名称' }, { max: NAME_MAX_LENGTH, message: `客户名称最长${NAME_MAX_LENGTH}个字符` }]}>
            <Input allowClear maxLength={NAME_MAX_LENGTH} />
          </Form.Item>
          <Form.Item label="简称" name="shortName" normalize={normalizeTextInput} rules={[{ max: SHORT_NAME_MAX_LENGTH, message: `简称最长${SHORT_NAME_MAX_LENGTH}个字符` }]}>
            <Input allowClear maxLength={SHORT_NAME_MAX_LENGTH} />
          </Form.Item>
          {formMode === 'create' ? (
            <Form.Item label="客户类型" name="type" rules={[{ required: true, message: '请选择客户类型' }]}>
              <Select options={customerTypeOptions.slice(1)} />
            </Form.Item>
          ) : null}
          <Form.Item label="客户等级" name="level">
            <Select options={levelOptions.slice(1)} />
          </Form.Item>
          <Form.Item label="联系人" name="contactPerson" normalize={normalizeTextInput} rules={[{ max: CONTACT_PERSON_MAX_LENGTH, message: `联系人最长${CONTACT_PERSON_MAX_LENGTH}个字符` }]}>
            <Input allowClear maxLength={CONTACT_PERSON_MAX_LENGTH} />
          </Form.Item>
          <Form.Item label="联系电话" name="contactPhone" normalize={normalizeTextInput} rules={[{ max: PHONE_MAX_LENGTH, message: `联系电话最长${PHONE_MAX_LENGTH}个字符` }, { pattern: phonePattern, message: '请输入正确的手机号' }]}>
            <Input allowClear maxLength={PHONE_MAX_LENGTH} />
          </Form.Item>
          <Form.Item label="地址" name="address" normalize={normalizeTextInput}>
            <Input allowClear />
          </Form.Item>
          <Form.Item label="默认发货地址" name="deliveryAddress" normalize={normalizeTextInput}>
            <Input allowClear />
          </Form.Item>
          <Form.Item label="结算方式" name="settlementType">
            <Select allowClear options={settlementTypeOptions} />
          </Form.Item>
          <Form.Item label="信用额度" name="creditLimit">
            <InputNumber className="system-number-input" min={0} precision={2} />
          </Form.Item>
          <Form.Item label="备注" name="remark" normalize={normalizeTextInput}>
            <Input.TextArea rows={2} allowClear />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}

interface CustomerActionPermissions {
  canActivate: boolean;
  canCreate: boolean;
  canDeactivate: boolean;
  canDelete: boolean;
  canUpdate: boolean;
}

function buildColumns(handlers: {
  actions: CustomerActionPermissions;
  onActivate: (customer: CustomerRecord) => void;
  onDeactivate: (customer: CustomerRecord) => void;
  onDelete: (customer: CustomerRecord) => void;
  onEdit: (customer: CustomerRecord) => void;
}): ColumnsType<CustomerRecord> {
  const columns: ColumnsType<CustomerRecord> = [
    { title: '客户编码', dataIndex: 'code' },
    { title: '客户名称', dataIndex: 'name' },
    { title: '类型', dataIndex: 'type', render: (value: unknown) => <Tag>{customerTypeLabel(value)}</Tag> },
    { title: '等级', dataIndex: 'level', render: (value: unknown) => (value ? `${value}级` : '-') },
    { title: '联系人', dataIndex: 'contactPerson', render: (value: unknown) => value || '-' },
    { title: '联系电话', dataIndex: 'contactPhone', render: (value: unknown) => value || '-' },
    { title: '状态', dataIndex: 'status', render: (value: unknown) => <StatusTag status={String(value)} /> },
    {
      title: '操作',
      dataIndex: 'actions',
      render: (_: unknown, customer: CustomerRecord) => <ActionButtons record={customer} actions={handlers.actions} handlers={handlers} />,
    },
  ];
  return columns.filter((column) => column.title !== '操作' || hasAnyAction(handlers.actions));
}

function ActionButtons({ record, actions, handlers }: { record: CustomerRecord; actions: CustomerActionPermissions; handlers: { onActivate: (record: CustomerRecord) => void; onDeactivate: (record: CustomerRecord) => void; onDelete: (record: CustomerRecord) => void; onEdit: (record: CustomerRecord) => void } }) {
  return (
    <Space>
      {actions.canUpdate ? <Button type="link" aria-label={`编辑 ${record.name}`} onClick={() => handlers.onEdit(record)}>编辑</Button> : null}
      {record.status === 'ACTIVE' && actions.canDeactivate ? (
        <Popconfirm title="确认停用该客户？" okText="确认停用" cancelText="取消" onConfirm={() => handlers.onDeactivate(record)}>
          <Button type="link" aria-label={`停用 ${record.name}`}>停用</Button>
        </Popconfirm>
      ) : null}
      {record.status === 'INACTIVE' && actions.canActivate ? (
        <Popconfirm title="确认启用该客户？" okText="确认启用" cancelText="取消" onConfirm={() => handlers.onActivate(record)}>
          <Button type="link" aria-label={`启用 ${record.name}`}>启用</Button>
        </Popconfirm>
      ) : null}
      {actions.canDelete ? (
        <Popconfirm title="确认删除该客户？" description="已被销售订单引用时后端会拒绝删除。" okText="确认删除" cancelText="取消" onConfirm={() => handlers.onDelete(record)}>
          <Button danger type="link" aria-label={`删除 ${record.name}`}>删除</Button>
        </Popconfirm>
      ) : null}
    </Space>
  );
}

function getCustomerActions(permissions: string[]): CustomerActionPermissions {
  return {
    canCreate: permissions.includes('master:customer:create'),
    canUpdate: permissions.includes('master:customer:update'),
    canActivate: permissions.includes('master:customer:activate'),
    canDeactivate: permissions.includes('master:customer:deactivate'),
    canDelete: permissions.includes('master:customer:delete'),
  };
}

function hasAnyAction(actions: CustomerActionPermissions): boolean {
  return actions.canUpdate || actions.canActivate || actions.canDeactivate || actions.canDelete;
}

function customerTypeLabel(value: unknown): string {
  return customerTypeOptions.find((option) => option.value === value)?.label ?? String(value || '-');
}

function StatusTag({ status }: { status: string }) {
  return status === 'ACTIVE' ? <Tag color="green">启用</Tag> : <Tag color="default">停用</Tag>;
}

function toCreatePayload(values: CustomerFormValues): CreateCustomerPayload {
  return removeEmptyFields(values) as CreateCustomerPayload;
}

function toUpdatePayload(values: CustomerFormValues): UpdateCustomerPayload {
  const payload: Partial<CustomerFormValues> = { ...values };
  delete payload.type;
  return removeEmptyFields(payload) as UpdateCustomerPayload;
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
