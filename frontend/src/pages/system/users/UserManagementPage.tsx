import { ReloadOutlined, SearchOutlined, UserAddOutlined } from '@ant-design/icons';
import { ProCard } from '@ant-design/pro-components';
import { App, Button, Form, Input, Modal, Popconfirm, Select, Space, Table, Tag } from 'antd';
import type { ColumnsType, TablePaginationConfig } from 'antd/es/table';
import { useCallback, useEffect, useState } from 'react';
import { EmptyState, ErrorState, LoadingState } from '@/components/state';
import { getCurrentUserPermissions } from '@/services/auth/authService';
import { getApiErrorMessage } from '@/services/http/apiClient';
import {
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

const statusOptions = [
  { label: '全部状态', value: '' },
  { label: '启用', value: 'ACTIVE' },
  { label: '停用', value: 'INACTIVE' },
];

type UserFormValues = CreateUserPayload & UpdateUserPayload;

type UserFormMode = 'create' | 'edit';

export function UserManagementPage() {
  const { message } = App.useApp();
  const [form] = Form.useForm<UserFormValues>();
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
  const [pageResult, setPageResult] = useState<PageResult<UserRecord> | null>(null);
  const [permissions, setPermissions] = useState<string[]>(() => getAuthSession()?.permissions ?? []);
  const userActionPermissions = {
    canCreate: hasPermission(permissions, USER_CREATE_PERMISSION),
    canUpdate: hasPermission(permissions, USER_UPDATE_PERMISSION),
    canDeactivate: hasPermission(permissions, USER_DEACTIVATE_PERMISSION),
  };

  const userColumns = buildUserColumns({
    permissions: userActionPermissions,
    onEdit: openEditForm,
    onDeactivate: handleDeactivate,
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

  const handleSaveUser = async () => {
    const values = await form.validateFields();
    setSaving(true);
    try {
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
                rules={[
                  { required: true, message: '请输入用户名' },
                  { max: 50, message: '用户名最长50个字符' },
                ]}
              >
                <Input autoComplete="username" />
              </Form.Item>
              <Form.Item
                label="初始密码"
                name="password"
                rules={[
                  { required: true, message: '请输入初始密码' },
                  { min: 8, max: 50, message: '密码长度8-50个字符' },
                  {
                    pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).+$/,
                    message: '密码必须包含大写字母、小写字母和数字',
                  },
                ]}
              >
                <Input.Password autoComplete="new-password" />
              </Form.Item>
            </>
          ) : null}
          <Form.Item label="姓名" name="realName" rules={[{ max: 50, message: '姓名最长50个字符' }]}>
            <Input />
          </Form.Item>
          <Form.Item label="手机号" name="phone" rules={[{ max: 20, message: '手机号最长20个字符' }]}>
            <Input />
          </Form.Item>
          <Form.Item label="邮箱" name="email" rules={[{ max: 100, message: '邮箱最长100个字符' }]}>
            <Input />
          </Form.Item>
          {formMode === 'edit' ? (
            <Form.Item label="状态" name="status">
              <Select options={statusOptions.filter((item) => item.value)} />
            </Form.Item>
          ) : null}
        </Form>
      </Modal>
    </div>
  );
}

interface UserActionPermissions {
  canUpdate: boolean;
  canDeactivate: boolean;
}

function buildUserColumns(actions: {
  permissions: UserActionPermissions;
  onEdit: (user: UserRecord) => void;
  onDeactivate: (user: UserRecord) => void;
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

  if (!actions.permissions.canUpdate && !actions.permissions.canDeactivate) {
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

function hasPermission(permissions: string[], permission: string): boolean {
  return permissions.includes(permission);
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
