import { PlusOutlined } from '@ant-design/icons';
import { ProCard } from '@ant-design/pro-components';
import { App, Button, Form, Input, InputNumber, Modal, Popconfirm, Select, Space, Table, Tag, TreeSelect } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useCallback, useEffect, useMemo, useState } from 'react';
import { EmptyState, ErrorState, LoadingState } from '@/components/state';
import { getCurrentUserPermissions } from '@/services/auth/authService';
import { getApiErrorMessage } from '@/services/http/apiClient';
import {
  createCategory,
  deleteCategory,
  listCategoryTree,
  updateCategory,
  type CategoryRecord,
  type CreateCategoryPayload,
  type UpdateCategoryPayload,
} from '@/services/master/categoryService';
import { getAuthSession, setAuthSession } from '@/shared/storage/authSessionStorage';

const CODE_MAX_LENGTH = 32;
const NAME_MAX_LENGTH = 64;

const statusOptions = [
  { label: '启用', value: 'ACTIVE' },
  { label: '停用', value: 'INACTIVE' },
];

type CategoryFormValues = CreateCategoryPayload & UpdateCategoryPayload;
type FormMode = 'create' | 'edit';

interface CategoryTreeOption {
  children?: CategoryTreeOption[];
  title: string;
  value: string;
}

export function CategoryManagementPage() {
  const { message } = App.useApp();
  const [form] = Form.useForm<CategoryFormValues>();
  const [categories, setCategories] = useState<CategoryRecord[]>([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');
  const [formOpen, setFormOpen] = useState(false);
  const [formMode, setFormMode] = useState<FormMode>('create');
  const [editingCategory, setEditingCategory] = useState<CategoryRecord | null>(null);
  const [permissions, setPermissions] = useState<string[]>(() => getAuthSession()?.permissions ?? []);
  const actions = getCategoryActions(permissions);
  const categoryTreeData = useMemo(() => buildCategoryTreeData(categories), [categories]);

  const loadCategories = useCallback(async () => {
    setLoading(true);
    setErrorMessage('');
    try {
      setCategories(await listCategoryTree());
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void loadCategories();
  }, [loadCategories]);

  useEffect(() => {
    void refreshPermissions();
  }, []);

  const openCreateForm = () => {
    setFormMode('create');
    setEditingCategory(null);
    form.resetFields();
    setFormOpen(true);
  };

  function openEditForm(category: CategoryRecord) {
    setFormMode('edit');
    setEditingCategory(category);
    form.setFieldsValue({
      code: category.code,
      name: category.name,
      sortOrder: category.sortOrder ?? 0,
      status: category.status,
    });
    setFormOpen(true);
  }

  const handleSave = async () => {
    try {
      const values = await form.validateFields();
      setSaving(true);
      if (formMode === 'create') {
        await createCategory(toCreatePayload(values));
        message.success('物料分类创建成功');
      } else if (editingCategory) {
        await updateCategory(editingCategory.id, toUpdatePayload(values));
        message.success('物料分类更新成功');
      }
      setFormOpen(false);
      await loadCategories();
    } catch (error) {
      if (!isFormValidationError(error)) {
        message.error(getApiErrorMessage(error));
      }
    } finally {
      setSaving(false);
    }
  };

  async function handleDelete(category: CategoryRecord) {
    try {
      await deleteCategory(category.id);
      message.success('物料分类已删除');
      await loadCategories();
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

  if (loading && categories.length === 0) {
    return <LoadingState message="正在加载物料分类" />;
  }

  if (errorMessage && categories.length === 0) {
    return <ErrorState message={errorMessage} onRetry={loadCategories} />;
  }

  return (
    <div className="system-page category-management-page">
      <section className="system-page-topbar">
        <div>
          <h1>物料分类</h1>
          <p>维护面料、辅料、包装等物料分类树，供物料主数据选择末级分类。</p>
        </div>
        {actions.canCreate ? (
          <Button type="primary" icon={<PlusOutlined />} aria-label="新建分类" onClick={openCreateForm}>
            新建分类
          </Button>
        ) : null}
      </section>

      {errorMessage ? <ErrorState message={errorMessage} onRetry={loadCategories} /> : null}

      <ProCard className="system-table-card" bordered>
        <Table<CategoryRecord>
          rowKey="id"
          columns={buildColumns({ actions, onDelete: handleDelete, onEdit: openEditForm })}
          dataSource={categories}
          loading={loading}
          pagination={false}
          defaultExpandAllRows
          locale={{ emptyText: <EmptyState message="暂无物料分类" /> }}
        />
      </ProCard>

      <Modal
        title={formMode === 'create' ? '新建物料分类' : '编辑物料分类'}
        open={formOpen}
        confirmLoading={saving}
        okText="保存"
        cancelText="取消"
        okButtonProps={{ 'aria-label': '保存' }}
        onCancel={() => setFormOpen(false)}
        onOk={handleSave}
        destroyOnHidden
        forceRender
      >
        <Form<CategoryFormValues> form={form} layout="vertical" preserve={false}>
          {formMode === 'create' ? (
            <Form.Item label="父级分类" name="parentId">
              <TreeSelect
                allowClear
                aria-label="父级分类"
                placeholder="不选则创建顶级分类"
                treeData={categoryTreeData}
                treeDefaultExpandAll
              />
            </Form.Item>
          ) : null}
          <Form.Item
            label="分类编码"
            name="code"
            normalize={normalizeTextInput}
            rules={[
              { required: true, whitespace: true, message: '请输入分类编码' },
              { max: CODE_MAX_LENGTH, message: `分类编码最长${CODE_MAX_LENGTH}个字符` },
            ]}
          >
            <Input allowClear maxLength={CODE_MAX_LENGTH} />
          </Form.Item>
          <Form.Item
            label="分类名称"
            name="name"
            normalize={normalizeTextInput}
            rules={[
              { required: true, whitespace: true, message: '请输入分类名称' },
              { max: NAME_MAX_LENGTH, message: `分类名称最长${NAME_MAX_LENGTH}个字符` },
            ]}
          >
            <Input allowClear maxLength={NAME_MAX_LENGTH} />
          </Form.Item>
          <Form.Item label="排序号" name="sortOrder">
            <InputNumber className="system-number-input" min={0} precision={0} />
          </Form.Item>
          {formMode === 'edit' ? (
            <Form.Item label="状态" name="status">
              <Select aria-label="状态" options={statusOptions} />
            </Form.Item>
          ) : null}
        </Form>
      </Modal>
    </div>
  );
}

interface CategoryActions {
  canCreate: boolean;
  canDelete: boolean;
  canUpdate: boolean;
}

function buildColumns(handlers: {
  actions: CategoryActions;
  onDelete: (category: CategoryRecord) => void;
  onEdit: (category: CategoryRecord) => void;
}): ColumnsType<CategoryRecord> {
  const columns: ColumnsType<CategoryRecord> = [
    { title: '分类编码', dataIndex: 'code' },
    { title: '分类名称', dataIndex: 'name' },
    { title: '层级', dataIndex: 'level', render: (value: unknown) => `第${value}级` },
    { title: '排序号', dataIndex: 'sortOrder', render: (value: unknown) => value ?? 0 },
    { title: '状态', dataIndex: 'status', render: (value: unknown) => <StatusTag status={String(value)} /> },
    {
      title: '操作',
      dataIndex: 'actions',
      render: (_: unknown, category: CategoryRecord) => (
        <Space>
          {handlers.actions.canUpdate ? (
            <Button type="link" aria-label={`编辑 ${category.name}`} onClick={() => handlers.onEdit(category)}>
              编辑
            </Button>
          ) : null}
          {handlers.actions.canDelete ? (
            <Popconfirm title="确认删除该分类？" okText="确认删除" cancelText="取消" onConfirm={() => handlers.onDelete(category)}>
              <Button danger type="link" aria-label={`删除 ${category.name}`}>
                删除
              </Button>
            </Popconfirm>
          ) : null}
        </Space>
      ),
    },
  ];
  return columns.filter((column) => column.title !== '操作' || hasAnyAction(handlers.actions));
}

function getCategoryActions(permissions: string[]): CategoryActions {
  return {
    canCreate: permissions.includes('master:category:create'),
    canUpdate: permissions.includes('master:category:update'),
    canDelete: permissions.includes('master:category:delete'),
  };
}

function hasAnyAction(actions: CategoryActions): boolean {
  return actions.canUpdate || actions.canDelete;
}

function StatusTag({ status }: { status: string }) {
  return status === 'ACTIVE' ? <Tag color="green">启用</Tag> : <Tag color="default">停用</Tag>;
}

function buildCategoryTreeData(categories: CategoryRecord[]): CategoryTreeOption[] {
  return categories.map((category) => ({
    title: category.name,
    value: category.id,
    children: buildCategoryTreeData(category.children ?? []),
  }));
}

function toCreatePayload(values: CategoryFormValues): CreateCategoryPayload {
  return removeEmptyFields(values) as CreateCategoryPayload;
}

function toUpdatePayload(values: CategoryFormValues): UpdateCategoryPayload {
  const payload: Partial<CategoryFormValues> = { ...values };
  delete payload.parentId;
  return removeEmptyFields(payload) as UpdateCategoryPayload;
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
