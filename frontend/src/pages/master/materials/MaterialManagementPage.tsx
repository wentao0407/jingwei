import { PlusOutlined, ReloadOutlined, SearchOutlined } from '@ant-design/icons';
import { ProCard } from '@ant-design/pro-components';
import { App, Button, Form, Input, InputNumber, Modal, Popconfirm, Select, Space, Table, Tag, TreeSelect } from 'antd';
import type { ColumnsType, TablePaginationConfig } from 'antd/es/table';
import { useCallback, useEffect, useMemo, useState } from 'react';
import { EmptyState, ErrorState, LoadingState } from '@/components/state';
import { getCurrentUserPermissions } from '@/services/auth/authService';
import { getApiErrorMessage } from '@/services/http/apiClient';
import { listCategoryTree, type CategoryRecord } from '@/services/master/categoryService';
import type { PageResult } from '@/services/master/customerService';
import {
  createMaterial,
  deactivateMaterial,
  getMaterialAttributeDefs,
  listMaterials,
  updateMaterial,
  type AttributeDefRecord,
  type CreateMaterialPayload,
  type MaterialQueryParams,
  type MaterialRecord,
  type UpdateMaterialPayload,
} from '@/services/master/materialService';
import { getAuthSession, setAuthSession } from '@/shared/storage/authSessionStorage';

const DEFAULT_PAGE_SIZE = 10;
const INITIAL_PAGE = 1;
const NAME_MAX_LENGTH = 128;
const UNIT_MAX_LENGTH = 16;

const materialTypeOptions = [
  { label: '全部类型', value: '' },
  { label: '面料', value: 'FABRIC' },
  { label: '辅料', value: 'TRIM' },
  { label: '包装', value: 'PACKAGING' },
];

const statusOptions = [
  { label: '全部状态', value: '' },
  { label: '启用', value: 'ACTIVE' },
  { label: '停用', value: 'INACTIVE' },
];

interface MaterialFormValues {
  categoryId?: string;
  extAttrs?: Record<string, unknown>;
  name?: string;
  remark?: string;
  status?: string;
  type?: string;
  unit?: string;
}
type FormMode = 'create' | 'edit';

interface CategoryTreeOption {
  children?: CategoryTreeOption[];
  title: string;
  value: string;
}

export function MaterialManagementPage() {
  const { message } = App.useApp();
  const [form] = Form.useForm<MaterialFormValues>();
  const [keywordInput, setKeywordInput] = useState('');
  const [keyword, setKeyword] = useState('');
  const [type, setType] = useState('');
  const [categoryId, setCategoryId] = useState('');
  const [status, setStatus] = useState('');
  const [currentPage, setCurrentPage] = useState(INITIAL_PAGE);
  const [pageSize, setPageSize] = useState(DEFAULT_PAGE_SIZE);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');
  const [pageResult, setPageResult] = useState<PageResult<MaterialRecord> | null>(null);
  const [categories, setCategories] = useState<CategoryRecord[]>([]);
  const [attributeDefs, setAttributeDefs] = useState<AttributeDefRecord[]>([]);
  const [formOpen, setFormOpen] = useState(false);
  const [formMode, setFormMode] = useState<FormMode>('create');
  const [editingMaterial, setEditingMaterial] = useState<MaterialRecord | null>(null);
  const [permissions, setPermissions] = useState<string[]>(() => getAuthSession()?.permissions ?? []);
  const actions = getMaterialActions(permissions);
  const categoryTreeData = useMemo(() => buildCategoryTreeData(categories), [categories]);
  const categoryNameById = useMemo(() => buildCategoryNameMap(categories), [categories]);

  const loadMaterials = useCallback(async () => {
    setLoading(true);
    setErrorMessage('');
    try {
      const query: MaterialQueryParams = {
        current: currentPage,
        size: pageSize,
        ...(keyword ? { keyword } : {}),
        ...(type ? { type } : {}),
        ...(categoryId ? { categoryId } : {}),
        ...(status ? { status } : {}),
      };
      const [materialPage, categoryTree] = await Promise.all([listMaterials(query), listCategoryTree()]);
      setPageResult(materialPage);
      setCategories(categoryTree);
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error));
    } finally {
      setLoading(false);
    }
  }, [currentPage, pageSize, keyword, type, categoryId, status]);

  useEffect(() => {
    void loadMaterials();
  }, [loadMaterials]);

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
    setEditingMaterial(null);
    setAttributeDefs([]);
    form.resetFields();
    setFormOpen(true);
  };

  async function openEditForm(material: MaterialRecord) {
    setFormMode('edit');
    setEditingMaterial(material);
    form.setFieldsValue(
      {
        name: material.name,
        categoryId: material.categoryId,
        unit: material.unit,
        extAttrs: material.extAttrs ?? {},
        remark: material.remark ?? '',
        status: material.status,
      } as unknown as Parameters<typeof form.setFieldsValue>[0],
    );
    setFormOpen(true);
    await loadAttributeDefs(material.type);
  }

  async function handleTypeChange(materialType: string) {
    form.setFieldsValue({ extAttrs: {} });
    await loadAttributeDefs(materialType);
  }

  async function loadAttributeDefs(materialType: string) {
    if (!materialType || !actions.canReadAttributeDefs) {
      setAttributeDefs([]);
      return;
    }

    try {
      setAttributeDefs(await getMaterialAttributeDefs(materialType));
    } catch (error) {
      message.error(getApiErrorMessage(error));
      setAttributeDefs([]);
    }
  }

  const handleSave = async () => {
    try {
      const values = await form.validateFields();
      setSaving(true);
      if (formMode === 'create') {
        await createMaterial(toCreatePayload(values));
        message.success('物料创建成功');
      } else if (editingMaterial) {
        await updateMaterial(editingMaterial.id, toUpdatePayload(values));
        message.success('物料更新成功');
      }
      setFormOpen(false);
      await loadMaterials();
    } catch (error) {
      if (!isFormValidationError(error)) {
        message.error(getApiErrorMessage(error));
      }
    } finally {
      setSaving(false);
    }
  };

  async function handleDeactivate(material: MaterialRecord) {
    try {
      await deactivateMaterial(material.id);
      message.success('物料已停用');
      await loadMaterials();
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
    return <LoadingState message="正在加载物料数据" />;
  }

  if (errorMessage && !pageResult) {
    return <ErrorState message={errorMessage} onRetry={loadMaterials} />;
  }

  return (
    <div className="system-page material-management-page">
      <section className="system-page-topbar">
        <div>
          <h1>物料管理</h1>
          <p>维护面料、辅料和包装物料主数据，供 BOM、采购和库存业务引用。</p>
        </div>
        {actions.canCreate ? (
          <Button type="primary" icon={<PlusOutlined />} aria-label="新建物料" onClick={openCreateForm}>
            新建物料
          </Button>
        ) : null}
      </section>

      <ProCard className="system-filter-card" bordered>
        <Space wrap>
          <Input
            allowClear
            placeholder="搜索物料编码/名称"
            value={keywordInput}
            onChange={(event) => setKeywordInput(event.target.value)}
            onPressEnter={handleSearch}
          />
          <Select aria-label="物料类型筛选" value={type} options={materialTypeOptions} onChange={(value) => { setType(value); setCurrentPage(INITIAL_PAGE); }} />
          <TreeSelect
            allowClear
            aria-label="物料分类筛选"
            placeholder="全部分类"
            value={categoryId || undefined}
            treeData={categoryTreeData}
            treeDefaultExpandAll
            onChange={(value) => {
              setCategoryId(value ?? '');
              setCurrentPage(INITIAL_PAGE);
            }}
          />
          <Select aria-label="状态" value={status} options={statusOptions} onChange={(value) => { setStatus(value); setCurrentPage(INITIAL_PAGE); }} />
          <Button type="primary" icon={<SearchOutlined />} onClick={handleSearch}>
            查询
          </Button>
          <Button icon={<ReloadOutlined />} onClick={loadMaterials}>
            刷新
          </Button>
        </Space>
      </ProCard>

      {errorMessage ? <ErrorState message={errorMessage} onRetry={loadMaterials} /> : null}

      <ProCard className="system-table-card" bordered>
        <Table<MaterialRecord>
          rowKey="id"
          columns={buildColumns({
            actions,
            categoryNameById,
            onDeactivate: handleDeactivate,
            onEdit: openEditForm,
          })}
          dataSource={pageResult?.records ?? []}
          loading={loading}
          locale={{ emptyText: <EmptyState message="暂无物料数据" /> }}
          pagination={{ current: currentPage, pageSize, total: pageResult?.total ?? 0, showSizeChanger: true }}
          onChange={handleTableChange}
        />
      </ProCard>

      <Modal
        title={formMode === 'create' ? '新建物料' : '编辑物料'}
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
        <Form<MaterialFormValues> form={form} layout="vertical" preserve={false}>
          <Form.Item
            label="物料名称"
            name="name"
            normalize={normalizeTextInput}
            rules={[
              { required: true, whitespace: true, message: '请输入物料名称' },
              { max: NAME_MAX_LENGTH, message: `物料名称最长${NAME_MAX_LENGTH}个字符` },
            ]}
          >
            <Input allowClear maxLength={NAME_MAX_LENGTH} />
          </Form.Item>
          {formMode === 'create' ? (
            <Form.Item label="物料类型" name="type" rules={[{ required: true, message: '请选择物料类型' }]}>
              <Select aria-label="物料类型" options={materialTypeOptions.slice(1)} onChange={handleTypeChange} />
            </Form.Item>
          ) : (
            <Form.Item label="物料类型">
              <Input disabled value={materialTypeLabel(editingMaterial?.type)} />
            </Form.Item>
          )}
          <Form.Item label="物料分类" name="categoryId" rules={[{ required: true, message: '请选择物料分类' }]}>
            <TreeSelect aria-label="物料分类" treeData={categoryTreeData} treeDefaultExpandAll />
          </Form.Item>
          <Form.Item
            label="基本单位"
            name="unit"
            normalize={normalizeTextInput}
            rules={[
              { required: true, whitespace: true, message: '请输入基本单位' },
              { max: UNIT_MAX_LENGTH, message: `基本单位最长${UNIT_MAX_LENGTH}个字符` },
            ]}
          >
            <Input allowClear maxLength={UNIT_MAX_LENGTH} />
          </Form.Item>
          {attributeDefs.map((attributeDef) => (
            <DynamicAttributeField key={attributeDef.id} attributeDef={attributeDef} />
          ))}
          {formMode === 'edit' ? (
            <Form.Item label="状态" name="status">
              <Select options={statusOptions.slice(1)} />
            </Form.Item>
          ) : null}
          <Form.Item label="备注" name="remark" normalize={normalizeTextInput}>
            <Input.TextArea rows={2} allowClear />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}

interface MaterialActions {
  canCreate: boolean;
  canDeactivate: boolean;
  canReadAttributeDefs: boolean;
  canUpdate: boolean;
}

function buildColumns(handlers: {
  actions: MaterialActions;
  categoryNameById: Map<string, string>;
  onDeactivate: (material: MaterialRecord) => void;
  onEdit: (material: MaterialRecord) => void;
}): ColumnsType<MaterialRecord> {
  const columns: ColumnsType<MaterialRecord> = [
    { title: '物料编码', dataIndex: 'code' },
    { title: '物料名称', dataIndex: 'name' },
    { title: '类型', dataIndex: 'type', render: (value: unknown) => <Tag>{materialTypeLabel(value)}</Tag> },
    { title: '分类', dataIndex: 'categoryId', render: (value: unknown) => handlers.categoryNameById.get(String(value)) ?? '-' },
    { title: '单位', dataIndex: 'unit' },
    { title: '状态', dataIndex: 'status', render: (value: unknown) => <StatusTag status={String(value)} /> },
    {
      title: '操作',
      dataIndex: 'actions',
      render: (_: unknown, material: MaterialRecord) => (
        <Space>
          {handlers.actions.canUpdate ? (
            <Button type="link" aria-label={`编辑 ${material.name}`} onClick={() => handlers.onEdit(material)}>
              编辑
            </Button>
          ) : null}
          {material.status === 'ACTIVE' && handlers.actions.canDeactivate ? (
            <Popconfirm title="确认停用该物料？" okText="确认停用" cancelText="取消" onConfirm={() => handlers.onDeactivate(material)}>
              <Button type="link" aria-label={`停用 ${material.name}`}>
                停用
              </Button>
            </Popconfirm>
          ) : null}
        </Space>
      ),
    },
  ];
  return columns.filter((column) => column.title !== '操作' || hasAnyAction(handlers.actions));
}

function DynamicAttributeField({ attributeDef }: { attributeDef: AttributeDefRecord }) {
  const rules = attributeDef.required ? [{ required: true, message: `请输入${attributeDef.name}` }] : [];
  const name = ['extAttrs', attributeDef.code];

  if (attributeDef.inputType === 'NUMBER') {
    return (
      <Form.Item label={attributeDef.name} name={name} rules={rules}>
        <InputNumber className="system-number-input" />
      </Form.Item>
    );
  }

  if (attributeDef.inputType === 'SELECT') {
    return (
      <Form.Item label={attributeDef.name} name={name} rules={rules}>
        <Select options={(attributeDef.options ?? []).map((option) => ({ label: option, value: option }))} />
      </Form.Item>
    );
  }

  if (attributeDef.inputType === 'MULTI_SELECT') {
    return (
      <Form.Item label={attributeDef.name} name={name} rules={rules}>
        <Select mode="multiple" options={(attributeDef.options ?? []).map((option) => ({ label: option, value: option }))} />
      </Form.Item>
    );
  }

  return (
    <Form.Item label={attributeDef.name} name={name} normalize={normalizeTextInput} rules={rules}>
      <Input allowClear />
    </Form.Item>
  );
}

function getMaterialActions(permissions: string[]): MaterialActions {
  return {
    canCreate: permissions.includes('master:material:create'),
    canUpdate: permissions.includes('master:material:update'),
    canDeactivate: permissions.includes('master:material:deactivate'),
    canReadAttributeDefs: permissions.includes('master:material:attributeDefs'),
  };
}

function hasAnyAction(actions: MaterialActions): boolean {
  return actions.canUpdate || actions.canDeactivate;
}

function StatusTag({ status }: { status: string }) {
  return status === 'ACTIVE' ? <Tag color="green">启用</Tag> : <Tag color="default">停用</Tag>;
}

function materialTypeLabel(value: unknown): string {
  return materialTypeOptions.find((option) => option.value === value)?.label ?? String(value || '-');
}

function buildCategoryTreeData(categories: CategoryRecord[]): CategoryTreeOption[] {
  return categories.map((category) => ({
    title: category.name,
    value: category.id,
    children: buildCategoryTreeData(category.children ?? []),
  }));
}

function buildCategoryNameMap(categories: CategoryRecord[]): Map<string, string> {
  const result = new Map<string, string>();
  categories.forEach((category) => {
    result.set(category.id, category.name);
    buildCategoryNameMap(category.children ?? []).forEach((name, id) => result.set(id, name));
  });
  return result;
}

function toCreatePayload(values: MaterialFormValues): CreateMaterialPayload {
  return removeEmptyFields({
    name: values.name,
    type: values.type,
    categoryId: values.categoryId,
    unit: values.unit,
    extAttrs: removeEmptyFields(values.extAttrs ?? {}),
    remark: values.remark,
  }) as CreateMaterialPayload;
}

function toUpdatePayload(values: MaterialFormValues): UpdateMaterialPayload {
  return removeEmptyFields({
    name: values.name,
    categoryId: values.categoryId,
    unit: values.unit,
    extAttrs: removeEmptyFields(values.extAttrs ?? {}),
    remark: values.remark,
    status: values.status,
  }) as UpdateMaterialPayload;
}

function removeEmptyFields<T extends object>(payload: T): Partial<T> {
  return Object.fromEntries(
    Object.entries(payload)
      .map(([key, value]) => [key, typeof value === 'string' ? value.trim() : value])
      .filter(([, value]) => value !== undefined && value !== null && value !== ''),
  ) as Partial<T>;
}

function isFormValidationError(error: unknown): boolean {
  return typeof error === 'object' && error !== null && 'errorFields' in error;
}

function normalizeTextInput(value: unknown): unknown {
  return typeof value === 'string' ? value.trim() : value;
}
