import { PlusOutlined } from '@ant-design/icons';
import { ProCard } from '@ant-design/pro-components';
import { App, Button, Form, Input, InputNumber, Modal, Popconfirm, Select, Space, Table, Tag } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useCallback, useEffect, useState } from 'react';
import { EmptyState, ErrorState, LoadingState } from '@/components/state';
import { getCurrentUserPermissions } from '@/services/auth/authService';
import { getApiErrorMessage } from '@/services/http/apiClient';
import {
  createMenu,
  deleteMenu,
  listMenus,
  updateMenu,
  type CreateMenuPayload,
  type MenuRecord,
  type UpdateMenuPayload,
} from '@/services/system/menuService';
import { getAuthSession, setAuthSession } from '@/shared/storage/authSessionStorage';

const MENU_CREATE_PERMISSION = 'system:menu:create';
const MENU_UPDATE_PERMISSION = 'system:menu:update';
const MENU_DELETE_PERMISSION = 'system:menu:delete';
const ROOT_PARENT_ID = '0';
const DEFAULT_SORT_ORDER = 0;
const NAME_MAX_LENGTH = 100;
const PATH_MAX_LENGTH = 200;
const COMPONENT_MAX_LENGTH = 200;
const PERMISSION_MAX_LENGTH = 100;
const ICON_MAX_LENGTH = 100;

const menuTypeOptions = [
  { label: '目录', value: 'DIRECTORY' },
  { label: '菜单', value: 'MENU' },
  { label: '按钮', value: 'BUTTON' },
];

const visibleOptions = [
  { label: '显示', value: true },
  { label: '隐藏', value: false },
];

const statusOptions = [
  { label: '启用', value: 'ACTIVE' },
  { label: '停用', value: 'INACTIVE' },
];

const nameRules = [
  { required: true, whitespace: true, message: '请输入菜单名称' },
  { max: NAME_MAX_LENGTH, message: `菜单名称最长${NAME_MAX_LENGTH}个字符` },
];

const parentIdRules = [{ required: true, whitespace: true, message: '请输入父菜单ID' }];

const typeRules = [{ required: true, message: '请选择菜单类型' }];

const permissionRules = [
  ({ getFieldValue }: { getFieldValue: (name: string) => unknown }) => ({
    validator(_: unknown, value?: string) {
      if (getFieldValue('type') === 'BUTTON' && !value?.trim()) {
        return Promise.reject(new Error('请输入权限标识'));
      }

      return Promise.resolve();
    },
  }),
  { max: PERMISSION_MAX_LENGTH, message: `权限标识最长${PERMISSION_MAX_LENGTH}个字符` },
];

type MenuFormValues = CreateMenuPayload & UpdateMenuPayload;

type MenuFormMode = 'create' | 'edit';

export function MenuManagementPage() {
  const { message } = App.useApp();
  const [form] = Form.useForm<MenuFormValues>();
  const [menus, setMenus] = useState<MenuRecord[]>([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');
  const [formMode, setFormMode] = useState<MenuFormMode>('create');
  const [editingMenu, setEditingMenu] = useState<MenuRecord | null>(null);
  const [formOpen, setFormOpen] = useState(false);
  const [permissions, setPermissions] = useState<string[]>(() => getAuthSession()?.permissions ?? []);
  const menuActionPermissions = {
    canCreate: hasPermission(permissions, MENU_CREATE_PERMISSION),
    canUpdate: hasPermission(permissions, MENU_UPDATE_PERMISSION),
    canDelete: hasPermission(permissions, MENU_DELETE_PERMISSION),
  };
  const menuColumns = buildMenuColumns({
    permissions: menuActionPermissions,
    onDelete: handleDeleteMenu,
    onEdit: openEditForm,
  });

  const loadMenus = useCallback(async () => {
    setLoading(true);
    setErrorMessage('');
    try {
      setMenus(await listMenus());
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void loadMenus();
  }, [loadMenus]);

  useEffect(() => {
    void refreshPermissions();
  }, []);

  const openCreateForm = () => {
    setFormMode('create');
    setEditingMenu(null);
    form.setFieldsValue({
      parentId: ROOT_PARENT_ID,
      type: 'MENU',
      sortOrder: DEFAULT_SORT_ORDER,
      visible: true,
    });
    setFormOpen(true);
  };

  function openEditForm(menu: MenuRecord) {
    setFormMode('edit');
    setEditingMenu(menu);
    form.setFieldsValue({
      parentId: menu.parentId,
      name: menu.name,
      type: menu.type,
      path: menu.path ?? '',
      component: menu.component ?? '',
      permission: menu.permission ?? '',
      icon: menu.icon ?? '',
      sortOrder: menu.sortOrder ?? DEFAULT_SORT_ORDER,
      visible: menu.visible ?? true,
      status: menu.status ?? 'ACTIVE',
    });
    setFormOpen(true);
  }

  const closeForm = () => {
    if (saving) {
      return;
    }

    setFormOpen(false);
  };

  const handleSaveMenu = async () => {
    try {
      const values = await form.validateFields();
      setSaving(true);
      if (formMode === 'create') {
        await createMenu(toCreatePayload(values));
        message.success('菜单创建成功');
      } else if (editingMenu) {
        await updateMenu(editingMenu.id, toUpdatePayload(values));
        message.success('菜单更新成功');
      }
      setFormOpen(false);
      await loadMenus();
    } catch (error) {
      if (isFormValidationError(error)) {
        return;
      }

      message.error(getApiErrorMessage(error));
    } finally {
      setSaving(false);
    }
  };

  async function handleDeleteMenu(menu: MenuRecord) {
    try {
      await deleteMenu(menu.id);
      message.success('菜单删除成功');
      await loadMenus();
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
        setAuthSession({
          ...session,
          permissions: response.permissions,
          menuTree: response.menuTree,
        });
      }
    } catch {
      setPermissions(getAuthSession()?.permissions ?? []);
    }
  }

  if (loading && menus.length === 0) {
    return <LoadingState message="正在加载菜单数据" />;
  }

  if (errorMessage && menus.length === 0) {
    return <ErrorState message={errorMessage} onRetry={loadMenus} />;
  }

  return (
    <div className="system-page menu-management-page">
      <section className="system-page-topbar">
        <div>
          <h1>菜单管理</h1>
          <p>维护导航菜单、按钮权限点和前端路由配置。</p>
        </div>
        {menuActionPermissions.canCreate ? (
          <Button type="primary" icon={<PlusOutlined />} aria-label="新建菜单" onClick={openCreateForm}>
            新建菜单
          </Button>
        ) : null}
      </section>

      {errorMessage ? <ErrorState message={errorMessage} onRetry={loadMenus} /> : null}

      <ProCard className="system-table-card" bordered>
        <Table<MenuRecord>
          rowKey="id"
          columns={menuColumns}
          dataSource={menus}
          loading={loading}
          locale={{
            emptyText: <EmptyState message="暂无菜单数据" />,
          }}
          expandable={{ defaultExpandAllRows: true }}
          pagination={false}
        />
      </ProCard>
      <Modal
        title={formMode === 'create' ? '新建菜单' : '编辑菜单'}
        open={formOpen}
        confirmLoading={saving}
        okText="保存"
        cancelText="取消"
        okButtonProps={{ 'aria-label': '保存' }}
        onCancel={closeForm}
        onOk={handleSaveMenu}
        destroyOnHidden
        forceRender
      >
        <Form<MenuFormValues> form={form} layout="vertical" preserve={false}>
          <Form.Item label="菜单名称" name="name" normalize={normalizeTextInput} rules={nameRules}>
            <Input allowClear maxLength={NAME_MAX_LENGTH} placeholder="请输入菜单名称" />
          </Form.Item>
          <Form.Item label="菜单类型" name="type" rules={typeRules}>
            <Select options={menuTypeOptions} />
          </Form.Item>
          <Form.Item label="父菜单ID" name="parentId" normalize={normalizeTextInput} rules={parentIdRules}>
            <Input allowClear placeholder="0 表示顶级菜单" />
          </Form.Item>
          <Form.Item label="路由路径" name="path" normalize={normalizeTextInput}>
            <Input allowClear maxLength={PATH_MAX_LENGTH} placeholder="请输入路由路径" />
          </Form.Item>
          <Form.Item label="前端组件" name="component" normalize={normalizeTextInput}>
            <Input allowClear maxLength={COMPONENT_MAX_LENGTH} placeholder="请输入前端组件路径" />
          </Form.Item>
          <Form.Item label="权限标识" name="permission" normalize={normalizeTextInput} rules={permissionRules}>
            <Input allowClear maxLength={PERMISSION_MAX_LENGTH} placeholder="按钮类型必填" />
          </Form.Item>
          <Form.Item label="图标" name="icon" normalize={normalizeTextInput}>
            <Input allowClear maxLength={ICON_MAX_LENGTH} placeholder="请输入 Ant Design 图标名" />
          </Form.Item>
          <Form.Item label="排序号" name="sortOrder">
            <InputNumber className="system-number-input" min={0} precision={0} />
          </Form.Item>
          <Form.Item label="是否可见" name="visible">
            <Select options={visibleOptions} />
          </Form.Item>
          {formMode === 'edit' ? (
            <Form.Item label="状态" name="status">
              <Select options={statusOptions} />
            </Form.Item>
          ) : null}
        </Form>
      </Modal>
    </div>
  );
}

interface MenuActionPermissions {
  canDelete: boolean;
  canUpdate: boolean;
}

function buildMenuColumns(actions: {
  permissions: MenuActionPermissions;
  onDelete: (menu: MenuRecord) => void;
  onEdit: (menu: MenuRecord) => void;
}): ColumnsType<MenuRecord> {
  const columns: ColumnsType<MenuRecord> = [
    {
      title: '菜单名称',
      dataIndex: 'name',
    },
    {
      title: '类型',
      dataIndex: 'type',
      render: (type) => <Tag>{String(type)}</Tag>,
    },
    {
      title: '路由路径',
      dataIndex: 'path',
      render: (value) => value || '-',
    },
    {
      title: '权限标识',
      dataIndex: 'permission',
      render: (value) => value || '-',
    },
    {
      title: '可见',
      dataIndex: 'visible',
      render: (visible) => (visible === false ? '隐藏' : '显示'),
    },
    {
      title: '状态',
      dataIndex: 'status',
      render: (status) => <MenuStatusTag status={String(status || 'ACTIVE')} />,
    },
    {
      title: '排序',
      dataIndex: 'sortOrder',
      render: (value) => value ?? '-',
    },
  ];

  if (!actions.permissions.canUpdate && !actions.permissions.canDelete) {
    return columns;
  }

  return [
    ...columns,
    {
      title: '操作',
      dataIndex: 'actions',
      render: (_, menu) => (
        <Space>
          {actions.permissions.canUpdate ? (
            <Button type="link" aria-label={`编辑 ${menu.name}`} onClick={() => actions.onEdit(menu)}>
              编辑
            </Button>
          ) : null}
          {actions.permissions.canDelete ? (
            <Popconfirm
              title="确认删除该菜单？"
              description="存在子菜单时后端会拒绝删除。"
              okText="确认删除"
              cancelText="取消"
              onConfirm={() => actions.onDelete(menu)}
            >
              <Button danger type="link" aria-label={`删除 ${menu.name}`}>
                删除
              </Button>
            </Popconfirm>
          ) : null}
        </Space>
      ),
    },
  ];
}

function MenuStatusTag({ status }: { status: string }) {
  if (status === 'ACTIVE') {
    return <Tag color="green">启用</Tag>;
  }

  if (status === 'INACTIVE') {
    return <Tag color="default">停用</Tag>;
  }

  return <Tag>{status}</Tag>;
}

function hasPermission(permissions: string[], permission: string): boolean {
  return permissions.includes(permission);
}

function isFormValidationError(error: unknown): boolean {
  return typeof error === 'object' && error !== null && 'errorFields' in error;
}

function toCreatePayload(values: MenuFormValues): CreateMenuPayload {
  return removeEmptyFields({
    parentId: values.parentId,
    name: values.name,
    type: values.type,
    path: values.path,
    component: values.component,
    permission: values.permission,
    icon: values.icon,
    sortOrder: values.sortOrder,
    visible: values.visible,
  });
}

function toUpdatePayload(values: MenuFormValues): UpdateMenuPayload {
  return removeEmptyFields({
    parentId: values.parentId,
    name: values.name,
    type: values.type,
    path: values.path,
    component: values.component,
    permission: values.permission,
    icon: values.icon,
    sortOrder: values.sortOrder,
    visible: values.visible,
    status: values.status,
  });
}

function removeEmptyFields<T extends Record<string, unknown>>(payload: T): T {
  return Object.fromEntries(
    Object.entries(payload)
      .map(([key, value]) => [key, typeof value === 'string' ? value.trim() : value])
      .filter(([, value]) => value !== undefined && value !== null && value !== ''),
  ) as T;
}

function normalizeTextInput(value: unknown): unknown {
  return typeof value === 'string' ? value.trim() : value;
}
