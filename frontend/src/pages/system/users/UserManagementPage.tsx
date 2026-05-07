import { ReloadOutlined, SearchOutlined, TeamOutlined, UserAddOutlined } from '@ant-design/icons';
import { ProCard } from '@ant-design/pro-components';
import { App, Button, Form, Input, Modal, Popconfirm, Select, Space, Table, Tag } from 'antd';
import type { ColumnsType, TablePaginationConfig } from 'antd/es/table';
import { useCallback, useEffect, useState } from 'react';
import { EmptyState, ErrorState, LoadingState } from '@/components/state';
import { getCurrentUserPermissions } from '@/services/auth/authService';
import { getApiErrorMessage } from '@/services/http/apiClient';
import { listRoles, type RoleRecord } from '@/services/system/roleService';
import {
  assignUserRoles,
  createUser,
  deactivateUser,
  listUsers,
  updateUser,
  type CreateUserPayload,
  type PageResult,
  type UpdateUserPayload,
  type UserRecord,
} from '@/services/system/userService';
import { getAuthSession, setAuthSession } from '@/shared/storage/authSessionStorage';

const DEFAULT_PAGE_SIZE = 10;
const INITIAL_PAGE = 1;
const USER_CREATE_PERMISSION = 'system:user:create';
const USER_UPDATE_PERMISSION = 'system:user:update';
const USER_DEACTIVATE_PERMISSION = 'system:user:deactivate';
const USER_ASSIGN_ROLE_PERMISSION = 'system:user:assignRole';
const ROLE_OPTION_PAGE_SIZE = 100;
const USERNAME_MAX_LENGTH = 50;
const PASSWORD_MIN_LENGTH = 8;
const PASSWORD_MAX_LENGTH = 50;
const REAL_NAME_MAX_LENGTH = 50;
const PHONE_MAX_LENGTH = 20;
const EMAIL_MAX_LENGTH = 100;
const usernamePattern = /^[A-Za-z0-9_-]+$/;
const passwordPattern = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).+$/;
const phonePattern = /^1[3-9]\d{9}$/;

const statusOptions = [
  { label: '全部状态', value: '' },
  { label: '启用', value: 'ACTIVE' },
  { label: '停用', value: 'INACTIVE' },
];

const usernameRules = [
  { required: true, whitespace: true, message: '请输入用户名' },
  { max: USERNAME_MAX_LENGTH, message: `用户名最长${USERNAME_MAX_LENGTH}个字符` },
  {
    pattern: usernamePattern,
    message: '用户名只能包含字母、数字、下划线和短横线',
  },
];

const passwordRules = [
  { required: true, message: '请输入初始密码' },
  { min: PASSWORD_MIN_LENGTH, max: PASSWORD_MAX_LENGTH, message: '密码长度8-50个字符' },
  {
    pattern: passwordPattern,
    message: '密码必须包含大写字母、小写字母和数字',
  },
];

const realNameRules = [{ max: REAL_NAME_MAX_LENGTH, message: `姓名最长${REAL_NAME_MAX_LENGTH}个字符` }];

const phoneRules = [
  { max: PHONE_MAX_LENGTH, message: `手机号最长${PHONE_MAX_LENGTH}个字符` },
  {
    pattern: phonePattern,
    message: '请输入正确的手机号',
  },
];

const emailRules = [
  { max: EMAIL_MAX_LENGTH, message: `邮箱最长${EMAIL_MAX_LENGTH}个字符` },
  { type: 'email' as const, message: '请输入正确的邮箱' },
];

type UserFormValues = CreateUserPayload & UpdateUserPayload;

type UserFormMode = 'create' | 'edit';

interface RoleAssignmentFormValues {
  roleIds: string[];
}

export function UserManagementPage() {
  const { message } = App.useApp();
  const [form] = Form.useForm<UserFormValues>();
  const [roleForm] = Form.useForm<RoleAssignmentFormValues>();
  const [keywordInput, setKeywordInput] = useState('');
  const [keyword, setKeyword] = useState('');
  const [status, setStatus] = useState('');
  const [currentPage, setCurrentPage] = useState(INITIAL_PAGE);
  const [pageSize, setPageSize] = useState(DEFAULT_PAGE_SIZE);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');
  const [formMode, setFormMode] = useState<UserFormMode>('create');
  const [editingUser, setEditingUser] = useState<UserRecord | null>(null);
  const [formOpen, setFormOpen] = useState(false);
  const [assigningUser, setAssigningUser] = useState<UserRecord | null>(null);
  const [roleModalOpen, setRoleModalOpen] = useState(false);
  const [roleOptions, setRoleOptions] = useState<RoleRecord[]>([]);
  const [rolesLoading, setRolesLoading] = useState(false);
  const [rolesSaving, setRolesSaving] = useState(false);
  const [rolesErrorMessage, setRolesErrorMessage] = useState('');
  const [pageResult, setPageResult] = useState<PageResult<UserRecord> | null>(null);
  const [permissions, setPermissions] = useState<string[]>(() => getAuthSession()?.permissions ?? []);
  const userActionPermissions = {
    canCreate: hasPermission(permissions, USER_CREATE_PERMISSION),
    canUpdate: hasPermission(permissions, USER_UPDATE_PERMISSION),
    canDeactivate: hasPermission(permissions, USER_DEACTIVATE_PERMISSION),
    canAssignRole: hasPermission(permissions, USER_ASSIGN_ROLE_PERMISSION),
  };

  const userColumns = buildUserColumns({
    permissions: userActionPermissions,
    onEdit: openEditForm,
    onDeactivate: handleDeactivate,
    onAssignRole: openRoleAssignment,
  });

  const loadUsers = useCallback(async () => {
    setLoading(true);
    setErrorMessage('');
    try {
      const result = await listUsers({
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
    void loadUsers();
  }, [loadUsers]);

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
    setEditingUser(null);
    form.resetFields();
    setFormOpen(true);
  };

  function openEditForm(user: UserRecord) {
    setFormMode('edit');
    setEditingUser(user);
    form.setFieldsValue({
      realName: user.realName ?? '',
      phone: user.phone ?? '',
      email: user.email ?? '',
      status: user.status,
    });
    setFormOpen(true);
  }

  const closeForm = () => {
    if (saving) {
      return;
    }

    setFormOpen(false);
  };

  function openRoleAssignment(user: UserRecord) {
    setAssigningUser(user);
    setRolesErrorMessage('');
    setRoleModalOpen(true);
    void loadRoleOptions();
  }

  const closeRoleAssignment = () => {
    if (rolesSaving) {
      return;
    }

    setRoleModalOpen(false);
  };

  const handleSaveUser = async () => {
    try {
      const values = await form.validateFields();
      setSaving(true);
      if (formMode === 'create') {
        await createUser(toCreatePayload(values));
        message.success('用户创建成功');
      } else if (editingUser) {
        await updateUser(editingUser.id, toUpdatePayload(values));
        message.success('用户更新成功');
      }
      setFormOpen(false);
      await loadUsers();
    } catch (error) {
      if (isFormValidationError(error)) {
        return;
      }

      message.error(getApiErrorMessage(error));
    } finally {
      setSaving(false);
    }
  };

  async function handleDeactivate(user: UserRecord) {
    try {
      await deactivateUser(user.id);
      message.success('用户已停用');
      await loadUsers();
    } catch (error) {
      message.error(getApiErrorMessage(error));
    }
  }

  async function loadRoleOptions() {
    setRolesLoading(true);
    setRolesErrorMessage('');
    try {
      const result = await listRoles({
        current: INITIAL_PAGE,
        size: ROLE_OPTION_PAGE_SIZE,
      });
      setRoleOptions(result.records);
    } catch (error) {
      setRolesErrorMessage(getApiErrorMessage(error));
    } finally {
      setRolesLoading(false);
    }
  }

  async function handleAssignRoles() {
    if (!assigningUser) {
      return;
    }

    try {
      const values = await roleForm.validateFields();
      setRolesSaving(true);
      await assignUserRoles(assigningUser.id, { roleIds: values.roleIds });
      message.success('角色分配成功');
      setRoleModalOpen(false);
      await loadUsers();
    } catch (error) {
      if (isFormValidationError(error)) {
        return;
      }

      message.error(getApiErrorMessage(error));
    } finally {
      setRolesSaving(false);
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
    return <LoadingState message="正在加载用户数据" />;
  }

  if (errorMessage && !pageResult) {
    return <ErrorState message={errorMessage} onRetry={loadUsers} />;
  }

  const users = pageResult?.records ?? [];

  return (
    <div className="system-page user-management-page">
      <section className="system-page-topbar">
        <div>
          <h1>用户管理</h1>
          <p>维护系统账号、状态和角色授权入口。</p>
        </div>
        {userActionPermissions.canCreate ? (
          <Button type="primary" icon={<UserAddOutlined />} aria-label="新建用户" onClick={openCreateForm}>
            新建用户
          </Button>
        ) : null}
      </section>

      <ProCard className="system-filter-card" bordered>
        <Space wrap>
          <Input
            allowClear
            className="system-search-input"
            prefix={<SearchOutlined />}
            placeholder="搜索用户名/姓名/手机号"
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
          <Button
            type="primary"
            icon={<SearchOutlined />}
            onClick={handleSearch}
            aria-label="查询"
          >
            查询
          </Button>
          <Button icon={<ReloadOutlined />} onClick={loadUsers}>
            刷新
          </Button>
        </Space>
      </ProCard>

      {errorMessage ? <ErrorState message={errorMessage} onRetry={loadUsers} /> : null}

      <ProCard className="system-table-card" bordered>
        <Table<UserRecord>
          rowKey="id"
          columns={userColumns}
          dataSource={users}
          loading={loading}
          locale={{
            emptyText: <EmptyState message="暂无用户数据" />,
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
        title={formMode === 'create' ? '新建用户' : '编辑用户'}
        open={formOpen}
        confirmLoading={saving}
        okText="保存"
        cancelText="取消"
        okButtonProps={{ 'aria-label': '保存' }}
        onCancel={closeForm}
        onOk={handleSaveUser}
        destroyOnHidden
      >
        <Form<UserFormValues> form={form} layout="vertical" preserve={false}>
          {formMode === 'create' ? (
            <>
              <Form.Item
                label="用户名"
                name="username"
                rules={usernameRules}
              >
                <Input
                  allowClear
                  autoComplete="username"
                  maxLength={USERNAME_MAX_LENGTH}
                  placeholder="请输入用户名"
                />
              </Form.Item>
              <Form.Item
                label="初始密码"
                name="password"
                rules={passwordRules}
              >
                <Input.Password
                  autoComplete="new-password"
                  maxLength={PASSWORD_MAX_LENGTH}
                  placeholder="请输入初始密码"
                />
              </Form.Item>
            </>
          ) : null}
          <Form.Item label="姓名" name="realName" rules={realNameRules}>
            <Input allowClear maxLength={REAL_NAME_MAX_LENGTH} placeholder="请输入姓名" />
          </Form.Item>
          <Form.Item label="手机号" name="phone" rules={phoneRules}>
            <Input
              allowClear
              autoComplete="tel"
              maxLength={PHONE_MAX_LENGTH}
              placeholder="请输入手机号"
              type="tel"
            />
          </Form.Item>
          <Form.Item label="邮箱" name="email" rules={emailRules}>
            <Input
              allowClear
              autoComplete="email"
              maxLength={EMAIL_MAX_LENGTH}
              placeholder="请输入邮箱"
              type="email"
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
        title={assigningUser ? `分配角色 - ${assigningUser.username}` : '分配角色'}
        open={roleModalOpen}
        confirmLoading={rolesSaving}
        okText="保存角色"
        cancelText="取消"
        okButtonProps={{ 'aria-label': '保存角色' }}
        onCancel={closeRoleAssignment}
        onOk={handleAssignRoles}
        destroyOnHidden
      >
        {rolesErrorMessage ? <ErrorState message={rolesErrorMessage} onRetry={loadRoleOptions} /> : null}
        <Form<RoleAssignmentFormValues>
          key={assigningUser?.id ?? 'role-assignment'}
          form={roleForm}
          initialValues={{ roleIds: assigningUser?.roleIds ?? [] }}
          layout="vertical"
          preserve={false}
        >
          <Form.Item
            label="角色"
            name="roleIds"
            rules={[{ required: true, message: '请选择角色' }]}
          >
            <Select
              loading={rolesLoading}
              mode="multiple"
              options={roleOptions.map(toRoleSelectOption)}
              placeholder="请选择角色"
            />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}

interface UserActionPermissions {
  canUpdate: boolean;
  canDeactivate: boolean;
  canAssignRole: boolean;
}

function buildUserColumns(actions: {
  permissions: UserActionPermissions;
  onEdit: (user: UserRecord) => void;
  onDeactivate: (user: UserRecord) => void;
  onAssignRole: (user: UserRecord) => void;
}): ColumnsType<UserRecord> {
  const columns: ColumnsType<UserRecord> = [
    {
      title: '用户名',
      dataIndex: 'username',
    },
    {
      title: '姓名',
      dataIndex: 'realName',
      render: (value) => value || '-',
    },
    {
      title: '手机号',
      dataIndex: 'phone',
      render: (value) => value || '-',
    },
    {
      title: '邮箱',
      dataIndex: 'email',
      render: (value) => value || '-',
    },
    {
      title: '状态',
      dataIndex: 'status',
      render: (status) => <UserStatusTag status={String(status)} />,
    },
    {
      title: '创建时间',
      dataIndex: 'createdAt',
      render: (value) => formatDateTime(String(value || '')),
    },
  ];

  if (!actions.permissions.canUpdate && !actions.permissions.canDeactivate && !actions.permissions.canAssignRole) {
    return columns;
  }

  return [
    ...columns,
    {
      title: '操作',
      dataIndex: 'actions',
      render: (_, user) => (
        <Space>
          {actions.permissions.canUpdate ? (
            <Button type="link" aria-label={`编辑 ${user.username}`} onClick={() => actions.onEdit(user)}>
              编辑
            </Button>
          ) : null}
          {actions.permissions.canAssignRole ? (
            <Button
              type="link"
              icon={<TeamOutlined />}
              aria-label={`分配角色 ${user.username}`}
              onClick={() => actions.onAssignRole(user)}
            >
              分配角色
            </Button>
          ) : null}
          {actions.permissions.canDeactivate && user.status === 'ACTIVE' ? (
            <Popconfirm
              title="确认停用该用户？"
              okText="确认停用"
              cancelText="取消"
              onConfirm={() => actions.onDeactivate(user)}
            >
              <Button danger type="link" aria-label={`停用 ${user.username}`}>
                停用
              </Button>
            </Popconfirm>
          ) : null}
        </Space>
      ),
    },
  ];
}

function toRoleSelectOption(role: RoleRecord) {
  return {
    label: `${role.roleName}（${role.roleCode}）`,
    value: role.id,
  };
}

function hasPermission(permissions: string[], permission: string): boolean {
  return permissions.includes(permission);
}

function isFormValidationError(error: unknown): boolean {
  return typeof error === 'object' && error !== null && 'errorFields' in error;
}

function toCreatePayload(values: UserFormValues): CreateUserPayload {
  return removeEmptyFields({
    username: values.username,
    password: values.password,
    realName: values.realName,
    phone: values.phone,
    email: values.email,
  });
}

function toUpdatePayload(values: UserFormValues): UpdateUserPayload {
  return removeEmptyFields({
    realName: values.realName,
    phone: values.phone,
    email: values.email,
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

function UserStatusTag({ status }: { status: string }) {
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
