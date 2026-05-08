import { PlusOutlined, ReloadOutlined, SafetyOutlined, SearchOutlined } from '@ant-design/icons';
import { ProCard } from '@ant-design/pro-components';
import { App, Button, Form, Input, Modal, Select, Space, Spin, Table, Tag, Tree } from 'antd';
import type { ColumnsType, TablePaginationConfig } from 'antd/es/table';
import type { DataNode } from 'antd/es/tree';
import type { Key } from 'react';
import { useCallback, useEffect, useState } from 'react';
import { EmptyState, ErrorState, LoadingState } from '@/components/state';
import { getCurrentUserPermissions } from '@/services/auth/authService';
import { getApiErrorMessage } from '@/services/http/apiClient';
import {
  assignMenuPermissions,
  getRoleMenuIds,
  listMenus,
  type MenuRecord,
} from '@/services/system/menuService';
import {
  createRole,
  listRoles,
  updateRole,
  type CreateRolePayload,
  type RoleRecord,
  type UpdateRolePayload,
} from '@/services/system/roleService';
import type { PageResult } from '@/services/system/userService';
import { getAuthSession, setAuthSession } from '@/shared/storage/authSessionStorage';

const DEFAULT_PAGE_SIZE = 10;
const INITIAL_PAGE = 1;
const ROLE_CREATE_PERMISSION = 'system:role:create';
const ROLE_UPDATE_PERMISSION = 'system:role:update';
const ROLE_ASSIGN_PERMISSION = 'system:role:assignPermission';
const ROLE_CODE_MAX_LENGTH = 50;
const ROLE_NAME_MAX_LENGTH = 100;
const ROLE_DESCRIPTION_MAX_LENGTH = 500;
const roleCodePattern = /^[A-Z0-9_]+$/;

const statusOptions = [
  { label: '全部状态', value: '' },
  { label: '启用', value: 'ACTIVE' },
  { label: '停用', value: 'INACTIVE' },
];

const roleCodeRules = [
  { required: true, whitespace: true, message: '请输入角色编码' },
  { max: ROLE_CODE_MAX_LENGTH, message: `角色编码最长${ROLE_CODE_MAX_LENGTH}个字符` },
  {
    pattern: roleCodePattern,
    message: '角色编码只能包含大写字母、数字和下划线',
  },
];

const roleNameRules = [
  { required: true, whitespace: true, message: '请输入角色名称' },
  { max: ROLE_NAME_MAX_LENGTH, message: `角色名称最长${ROLE_NAME_MAX_LENGTH}个字符` },
];

const roleDescriptionRules = [
  { max: ROLE_DESCRIPTION_MAX_LENGTH, message: `角色描述最长${ROLE_DESCRIPTION_MAX_LENGTH}个字符` },
];

type RoleFormValues = CreateRolePayload & UpdateRolePayload;

type RoleFormMode = 'create' | 'edit';

export function RoleManagementPage() {
  const { message } = App.useApp();
  const [form] = Form.useForm<RoleFormValues>();
  const [keywordInput, setKeywordInput] = useState('');
  const [keyword, setKeyword] = useState('');
  const [status, setStatus] = useState('');
  const [currentPage, setCurrentPage] = useState(INITIAL_PAGE);
  const [pageSize, setPageSize] = useState(DEFAULT_PAGE_SIZE);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');
  const [formMode, setFormMode] = useState<RoleFormMode>('create');
  const [editingRole, setEditingRole] = useState<RoleRecord | null>(null);
  const [formOpen, setFormOpen] = useState(false);
  const [permissionRole, setPermissionRole] = useState<RoleRecord | null>(null);
  const [permissionTree, setPermissionTree] = useState<MenuRecord[]>([]);
  const [checkedMenuIds, setCheckedMenuIds] = useState<string[]>([]);
  const [permissionOpen, setPermissionOpen] = useState(false);
  const [permissionLoading, setPermissionLoading] = useState(false);
  const [permissionSaving, setPermissionSaving] = useState(false);
  const [pageResult, setPageResult] = useState<PageResult<RoleRecord> | null>(null);
  const [permissions, setPermissions] = useState<string[]>(() => getAuthSession()?.permissions ?? []);
  const roleActionPermissions = {
    canCreate: hasPermission(permissions, ROLE_CREATE_PERMISSION),
    canUpdate: hasPermission(permissions, ROLE_UPDATE_PERMISSION),
    canAssignPermission: hasPermission(permissions, ROLE_ASSIGN_PERMISSION),
  };

  const roleColumns = buildRoleColumns({
    permissions: roleActionPermissions,
    onAssignPermission: openPermissionModal,
    onEdit: openEditForm,
  });

  const loadRoles = useCallback(async () => {
    setLoading(true);
    setErrorMessage('');
    try {
      const result = await listRoles({
        current: currentPage,
        size: pageSize,
        ...(keyword ? { keyword } : {}),
        ...(status ? { status } : {}),
      });
      setPageResult(result);
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error));
    } finally {
      setLoading(false);
    }
  }, [currentPage, pageSize, keyword, status]);

  useEffect(() => {
    void loadRoles();
  }, [loadRoles]);

  useEffect(() => {
    void refreshPermissions();
  }, []);

  const handleSearch = () => {
    setKeyword(keywordInput.trim());
    setCurrentPage(INITIAL_PAGE);
  };

  const handleStatusChange = (nextStatus: string) => {
    setStatus(nextStatus);
    setCurrentPage(INITIAL_PAGE);
  };

  const handleTableChange = (pagination: TablePaginationConfig) => {
    setCurrentPage(pagination.current ?? INITIAL_PAGE);
    setPageSize(pagination.pageSize ?? DEFAULT_PAGE_SIZE);
  };

  const openCreateForm = () => {
    setFormMode('create');
    setEditingRole(null);
    form.resetFields();
    setFormOpen(true);
  };

  function openEditForm(role: RoleRecord) {
    setFormMode('edit');
    setEditingRole(role);
    form.setFieldsValue({
      roleName: role.roleName,
      description: role.description ?? '',
      status: role.status,
    });
    setFormOpen(true);
  }

  const closeForm = () => {
    if (saving) {
      return;
    }

    setFormOpen(false);
  };

  const handleSaveRole = async () => {
    try {
      const values = await form.validateFields();
      setSaving(true);
      if (formMode === 'create') {
        await createRole(toCreatePayload(values));
        message.success('角色创建成功');
      } else if (editingRole) {
        await updateRole(editingRole.id, toUpdatePayload(values));
        message.success('角色更新成功');
      }
      setFormOpen(false);
      await loadRoles();
    } catch (error) {
      if (isFormValidationError(error)) {
        return;
      }

      message.error(getApiErrorMessage(error));
    } finally {
      setSaving(false);
    }
  };

  async function openPermissionModal(role: RoleRecord) {
    setPermissionRole(role);
    setPermissionTree([]);
    setCheckedMenuIds([]);
    setPermissionOpen(true);
    setPermissionLoading(true);
    try {
      const [menuTree, assignedMenuIds] = await Promise.all([listMenus(), getRoleMenuIds(role.id)]);
      setPermissionTree(menuTree);
      setCheckedMenuIds(assignedMenuIds);
    } catch (error) {
      message.error(getApiErrorMessage(error));
    } finally {
      setPermissionLoading(false);
    }
  }

  const closePermissionModal = () => {
    if (permissionSaving) {
      return;
    }

    setPermissionOpen(false);
  };

  async function handleSavePermissions() {
    if (!permissionRole) {
      return;
    }

    if (checkedMenuIds.length === 0) {
      message.error('请至少选择一个菜单权限');
      return;
    }

    try {
      setPermissionSaving(true);
      await assignMenuPermissions({
        roleId: permissionRole.id,
        menuIds: checkedMenuIds,
      });
      message.success('权限分配成功');
      setPermissionOpen(false);
    } catch (error) {
      message.error(getApiErrorMessage(error));
    } finally {
      setPermissionSaving(false);
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

  if (loading && !pageResult) {
    return <LoadingState message="正在加载角色数据" />;
  }

  if (errorMessage && !pageResult) {
    return <ErrorState message={errorMessage} onRetry={loadRoles} />;
  }

  const roles = pageResult?.records ?? [];

  return (
    <div className="system-page role-management-page">
      <section className="system-page-topbar">
        <div>
          <h1>角色管理</h1>
          <p>查看角色编码、名称、状态和维护时间。</p>
        </div>
        {roleActionPermissions.canCreate ? (
          <Button type="primary" icon={<PlusOutlined />} aria-label="新建角色" onClick={openCreateForm}>
            新建角色
          </Button>
        ) : null}
      </section>

      <ProCard className="system-filter-card" bordered>
        <Space wrap>
          <Input
            allowClear
            className="system-search-input"
            prefix={<SearchOutlined />}
            placeholder="搜索角色编码/角色名称"
            value={keywordInput}
            onChange={(event) => setKeywordInput(event.target.value)}
            onPressEnter={handleSearch}
          />
          <Select
            className="system-status-select"
            options={statusOptions}
            value={status}
            onChange={handleStatusChange}
          />
          <Button type="primary" icon={<SearchOutlined />} onClick={handleSearch} aria-label="查询">
            查询
          </Button>
          <Button icon={<ReloadOutlined />} onClick={loadRoles}>
            刷新
          </Button>
        </Space>
      </ProCard>

      {errorMessage ? <ErrorState message={errorMessage} onRetry={loadRoles} /> : null}

      <ProCard className="system-table-card" bordered>
        <Table<RoleRecord>
          rowKey="id"
          columns={roleColumns}
          dataSource={roles}
          loading={loading}
          locale={{
            emptyText: <EmptyState message="暂无角色数据" />,
          }}
          pagination={{
            current: pageResult?.current ?? currentPage,
            pageSize: pageResult?.size ?? pageSize,
            total: pageResult?.total ?? 0,
            showSizeChanger: true,
            showTotal: (total) => `共 ${total} 条`,
          }}
          onChange={handleTableChange}
        />
      </ProCard>
      <Modal
        title={formMode === 'create' ? '新建角色' : '编辑角色'}
        open={formOpen}
        confirmLoading={saving}
        okText="保存"
        cancelText="取消"
        okButtonProps={{ 'aria-label': '保存' }}
        onCancel={closeForm}
        onOk={handleSaveRole}
        destroyOnHidden
        forceRender
      >
        <Form<RoleFormValues> form={form} layout="vertical" preserve={false}>
          {formMode === 'create' ? (
            <Form.Item label="角色编码" name="roleCode" normalize={normalizeTextInput} rules={roleCodeRules}>
              <Input allowClear maxLength={ROLE_CODE_MAX_LENGTH} placeholder="请输入角色编码" />
            </Form.Item>
          ) : null}
          <Form.Item label="角色名称" name="roleName" normalize={normalizeTextInput} rules={roleNameRules}>
            <Input allowClear maxLength={ROLE_NAME_MAX_LENGTH} placeholder="请输入角色名称" />
          </Form.Item>
          <Form.Item label="角色描述" name="description" normalize={normalizeTextInput} rules={roleDescriptionRules}>
            <Input.TextArea
              allowClear
              maxLength={ROLE_DESCRIPTION_MAX_LENGTH}
              placeholder="请输入角色描述"
              rows={4}
              showCount
            />
          </Form.Item>
          {formMode === 'edit' ? (
            <Form.Item label="状态" name="status">
              <Select options={statusOptions.filter((item) => item.value)} />
            </Form.Item>
          ) : null}
        </Form>
      </Modal>
      <Modal
        title="分配权限"
        open={permissionOpen}
        confirmLoading={permissionSaving}
        okText="保存权限"
        cancelText="取消"
        okButtonProps={{ 'aria-label': '保存权限' }}
        onCancel={closePermissionModal}
        onOk={handleSavePermissions}
        destroyOnHidden
      >
        <p>{permissionRole ? `为 ${permissionRole.roleCode} 分配菜单和按钮权限` : '分配菜单和按钮权限'}</p>
        <Spin spinning={permissionLoading}>
          <Tree
            checkable
            checkedKeys={checkedMenuIds}
            expandedKeys={getAllMenuIds(permissionTree)}
            treeData={toPermissionTreeData(permissionTree)}
            onCheck={(keys) => setCheckedMenuIds(normalizeCheckedKeys(keys))}
          />
        </Spin>
      </Modal>
    </div>
  );
}

interface RoleActionPermissions {
  canAssignPermission: boolean;
  canUpdate: boolean;
}

function buildRoleColumns(actions: {
  permissions: RoleActionPermissions;
  onAssignPermission: (role: RoleRecord) => void;
  onEdit: (role: RoleRecord) => void;
}): ColumnsType<RoleRecord> {
  const columns: ColumnsType<RoleRecord> = [
    {
      title: '角色编码',
      dataIndex: 'roleCode',
    },
    {
      title: '角色名称',
      dataIndex: 'roleName',
    },
    {
      title: '描述',
      dataIndex: 'description',
      render: (value) => value || '-',
    },
    {
      title: '状态',
      dataIndex: 'status',
      render: (status) => <RoleStatusTag status={String(status)} />,
    },
    {
      title: '创建时间',
      dataIndex: 'createdAt',
      render: (value) => formatDateTime(String(value || '')),
    },
    {
      title: '更新时间',
      dataIndex: 'updatedAt',
      render: (value) => formatDateTime(String(value || '')),
    },
  ];

  if (!actions.permissions.canUpdate && !actions.permissions.canAssignPermission) {
    return columns;
  }

  return [
    ...columns,
    {
      title: '操作',
      dataIndex: 'actions',
      render: (_, role) => (
        <Space>
          {actions.permissions.canUpdate ? (
            <Button type="link" aria-label={`编辑 ${role.roleCode}`} onClick={() => actions.onEdit(role)}>
              编辑
            </Button>
          ) : null}
          {actions.permissions.canAssignPermission ? (
            <Button
              type="link"
              icon={<SafetyOutlined />}
              aria-label={`分配权限 ${role.roleCode}`}
              onClick={() => actions.onAssignPermission(role)}
            >
              分配权限
            </Button>
          ) : null}
        </Space>
      ),
    },
  ];
}

function toPermissionTreeData(menus: MenuRecord[]): DataNode[] {
  return menus.map((menu) => ({
    key: menu.id,
    title: menu.name,
    children: menu.children?.length ? toPermissionTreeData(menu.children) : undefined,
  }));
}

function normalizeCheckedKeys(keys: Key[] | { checked: Key[]; halfChecked: Key[] }): string[] {
  const checkedKeys = Array.isArray(keys) ? keys : keys.checked;
  return checkedKeys.map(String);
}

function getAllMenuIds(menus: MenuRecord[]): string[] {
  return menus.flatMap((menu) => [menu.id, ...getAllMenuIds(menu.children ?? [])]);
}

function hasPermission(permissions: string[], permission: string): boolean {
  return permissions.includes(permission);
}

function isFormValidationError(error: unknown): boolean {
  return typeof error === 'object' && error !== null && 'errorFields' in error;
}

function toCreatePayload(values: RoleFormValues): CreateRolePayload {
  return removeEmptyFields({
    roleCode: values.roleCode,
    roleName: values.roleName,
    description: values.description,
  });
}

function toUpdatePayload(values: RoleFormValues): UpdateRolePayload {
  return removeEmptyFields({
    roleName: values.roleName,
    description: values.description,
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

function RoleStatusTag({ status }: { status: string }) {
  if (status === 'ACTIVE') {
    return <Tag color="green">启用</Tag>;
  }

  if (status === 'INACTIVE') {
    return <Tag color="default">停用</Tag>;
  }

  return <Tag>{status}</Tag>;
}

function formatDateTime(value: string): string {
  if (!value) {
    return '-';
  }

  return value.replace('T', ' ').slice(0, 16);
}
