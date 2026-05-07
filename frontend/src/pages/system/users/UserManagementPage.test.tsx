import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { App as AntdApp } from 'antd';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { UserManagementPage } from './UserManagementPage';
import { getCurrentUserPermissions } from '@/services/auth/authService';
import { listRoles } from '@/services/system/roleService';
import { assignUserRoles, createUser, deactivateUser, listUsers, updateUser } from '@/services/system/userService';
import { setAuthSession, type AuthSession } from '@/shared/storage/authSessionStorage';

vi.mock('@/services/auth/authService', () => ({
  getCurrentUserPermissions: vi.fn(),
}));

vi.mock('@/services/system/userService', () => ({
  assignUserRoles: vi.fn(),
  createUser: vi.fn(),
  deactivateUser: vi.fn(),
  listUsers: vi.fn(),
  updateUser: vi.fn(),
}));

vi.mock('@/services/system/roleService', () => ({
  listRoles: vi.fn(),
}));

const mockedGetCurrentUserPermissions = vi.mocked(getCurrentUserPermissions);
const mockedAssignUserRoles = vi.mocked(assignUserRoles);
const mockedCreateUser = vi.mocked(createUser);
const mockedDeactivateUser = vi.mocked(deactivateUser);
const mockedListUsers = vi.mocked(listUsers);
const mockedListRoles = vi.mocked(listRoles);
const mockedUpdateUser = vi.mocked(updateUser);
const userActionPermissionCodes = [
  'system:user:create',
  'system:user:update',
  'system:user:deactivate',
  'system:user:assignRole',
];
const snowflakeUserId = '1778059952652742657';
const snowflakeRoleId = '1778059952652742666';

const activeAdminUser = {
  id: '1',
  username: 'admin',
  realName: '系统管理员',
  phone: '13800000000',
  email: 'admin@example.com',
  status: 'ACTIVE',
  roleIds: ['1'],
  createdAt: '2026-05-01T10:00:00',
  updatedAt: '2026-05-02T10:00:00',
};

describe('UserManagementPage', () => {
  beforeEach(() => {
    window.localStorage.clear();
    mockedGetCurrentUserPermissions.mockReset();
    mockedGetCurrentUserPermissions.mockResolvedValue({
      menuTree: [],
      permissions: userActionPermissionCodes,
    });
    mockedAssignUserRoles.mockReset();
    mockedCreateUser.mockReset();
    mockedDeactivateUser.mockReset();
    mockedListUsers.mockReset();
    mockedListRoles.mockReset();
    mockedUpdateUser.mockReset();
  });

  it('loads and renders paged users', async () => {
    mockedListUsers.mockResolvedValue({
      records: [activeAdminUser],
      total: 1,
      size: 10,
      current: 1,
      pages: 1,
    });

    renderPage();

    expect(screen.getByText('正在加载用户数据')).toBeInTheDocument();
    expect(await screen.findByText('系统管理员')).toBeInTheDocument();
    expect(screen.getByText('admin')).toBeInTheDocument();
    expect(screen.getByText('启用')).toBeInTheDocument();
    expect(mockedListUsers).toHaveBeenCalledWith({ current: 1, size: 10 });
  });

  it('shows empty state when there are no users', async () => {
    mockedListUsers.mockResolvedValue({
      records: [],
      total: 0,
      size: 10,
      current: 1,
      pages: 0,
    });

    renderPage();

    expect(await screen.findByText('暂无用户数据')).toBeInTheDocument();
  });

  it('shows backend error message and retries loading', async () => {
    mockedListUsers.mockRejectedValueOnce(new Error('用户加载失败'));
    mockedListUsers.mockResolvedValueOnce({
      records: [],
      total: 0,
      size: 10,
      current: 1,
      pages: 0,
    });

    renderPage();

    expect(await screen.findByText('用户加载失败')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: '重新加载' }));

    await waitFor(() => expect(mockedListUsers).toHaveBeenCalledTimes(2));
  });

  it('searches users by keyword', async () => {
    mockedListUsers.mockResolvedValue({
      records: [],
      total: 0,
      size: 10,
      current: 1,
      pages: 0,
    });

    renderPage();

    await screen.findByText('暂无用户数据');
    fireEvent.change(screen.getByPlaceholderText('搜索用户名/姓名/手机号'), {
      target: { value: 'admin' },
    });
    fireEvent.click(screen.getByRole('button', { name: '查询' }));

    await waitFor(() =>
      expect(mockedListUsers).toHaveBeenLastCalledWith({ current: 1, size: 10, keyword: 'admin' }),
    );
  });

  it('creates a user and reloads the list', async () => {
    mockedListUsers.mockResolvedValue({
      records: [],
      total: 0,
      size: 10,
      current: 1,
      pages: 0,
    });
    mockedCreateUser.mockResolvedValue({ ...activeAdminUser, id: '2', username: 'newuser' });

    renderPage();

    await screen.findByText('暂无用户数据');
    fireEvent.click(screen.getByRole('button', { name: '新建用户' }));
    fireEvent.change(screen.getByLabelText('用户名'), { target: { value: 'newuser' } });
    fireEvent.change(screen.getByLabelText('初始密码'), { target: { value: 'Example123' } });
    fireEvent.change(screen.getByLabelText('姓名'), { target: { value: '新用户' } });
    fireEvent.click(screen.getByRole('button', { name: '保存' }));

    await waitFor(() =>
      expect(mockedCreateUser).toHaveBeenCalledWith({
        username: 'newuser',
        password: 'Example123',
        realName: '新用户',
      }),
    );
    expect(mockedListUsers).toHaveBeenCalledTimes(2);
  });

  it('blocks creating users with invalid input format', async () => {
    mockedListUsers.mockResolvedValue({
      records: [],
      total: 0,
      size: 10,
      current: 1,
      pages: 0,
    });

    renderPage();

    await screen.findByText('暂无用户数据');
    fireEvent.click(screen.getByRole('button', { name: '新建用户' }));
    fireEvent.change(screen.getByLabelText('用户名'), { target: { value: 'bad user' } });
    fireEvent.change(screen.getByLabelText('初始密码'), { target: { value: 'Example123' } });
    fireEvent.change(screen.getByLabelText('手机号'), { target: { value: '12345' } });
    fireEvent.change(screen.getByLabelText('邮箱'), { target: { value: 'bad-email' } });
    fireEvent.click(screen.getByRole('button', { name: '保存' }));

    expect(await screen.findByText('用户名只能包含字母、数字、下划线和短横线')).toBeInTheDocument();
    expect(screen.getByText('请输入正确的手机号')).toBeInTheDocument();
    expect(screen.getByText('请输入正确的邮箱')).toBeInTheDocument();
    expect(mockedCreateUser).not.toHaveBeenCalled();
  });

  it('updates a user and reloads the list', async () => {
    mockedListUsers.mockResolvedValue({
      records: [activeAdminUser],
      total: 1,
      size: 10,
      current: 1,
      pages: 1,
    });
    mockedUpdateUser.mockResolvedValue({ ...activeAdminUser, realName: '管理员' });

    renderPage();

    expect(await screen.findByText('系统管理员')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: '编辑 admin' }));
    fireEvent.change(screen.getByLabelText('姓名'), { target: { value: '管理员' } });
    fireEvent.click(screen.getByRole('button', { name: '保存' }));

    await waitFor(() =>
      expect(mockedUpdateUser).toHaveBeenCalledWith('1', {
        realName: '管理员',
        phone: '13800000000',
        email: 'admin@example.com',
        status: 'ACTIVE',
      }),
    );
    expect(mockedListUsers).toHaveBeenCalledTimes(2);
  });

  it('blocks updating users with invalid contact format', async () => {
    mockedListUsers.mockResolvedValue({
      records: [activeAdminUser],
      total: 1,
      size: 10,
      current: 1,
      pages: 1,
    });

    renderPage();

    expect(await screen.findByText('系统管理员')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: '编辑 admin' }));
    fireEvent.change(screen.getByLabelText('手机号'), { target: { value: 'not-phone' } });
    fireEvent.change(screen.getByLabelText('邮箱'), { target: { value: 'admin@' } });
    fireEvent.click(screen.getByRole('button', { name: '保存' }));

    expect(await screen.findByText('请输入正确的手机号')).toBeInTheDocument();
    expect(screen.getByText('请输入正确的邮箱')).toBeInTheDocument();
    expect(mockedUpdateUser).not.toHaveBeenCalled();
  });

  it('deactivates a user after confirmation and reloads the list', async () => {
    mockedListUsers.mockResolvedValue({
      records: [activeAdminUser],
      total: 1,
      size: 10,
      current: 1,
      pages: 1,
    });
    mockedDeactivateUser.mockResolvedValue(undefined);

    renderPage();

    expect(await screen.findByText('系统管理员')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: '停用 admin' }));
    fireEvent.click(screen.getByRole('button', { name: '确认停用' }));

    await waitFor(() => expect(mockedDeactivateUser).toHaveBeenCalledWith('1'));
    expect(mockedListUsers).toHaveBeenCalledTimes(2);
  });

  it('keeps snowflake user ids as strings when updating and deactivating users', async () => {
    const snowflakeUser = {
      ...activeAdminUser,
      id: snowflakeUserId,
      username: 'snowflake',
      realName: '雪花用户',
    };
    mockedListUsers.mockResolvedValue({
      records: [snowflakeUser],
      total: 1,
      size: 10,
      current: 1,
      pages: 1,
    });
    mockedUpdateUser.mockResolvedValue({ ...snowflakeUser, realName: '雪花用户A' });
    mockedDeactivateUser.mockResolvedValue(undefined);

    renderPage();

    expect(await screen.findByText('雪花用户')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: '编辑 snowflake' }));
    fireEvent.change(screen.getByLabelText('姓名'), { target: { value: '雪花用户A' } });
    fireEvent.click(screen.getByRole('button', { name: '保存' }));

    await waitFor(() => expect(mockedUpdateUser).toHaveBeenCalledWith(snowflakeUserId, expect.any(Object)));

    fireEvent.click(screen.getByRole('button', { name: '停用 snowflake' }));
    fireEvent.click(screen.getByRole('button', { name: '确认停用' }));

    await waitFor(() => expect(mockedDeactivateUser).toHaveBeenCalledWith(snowflakeUserId));
  });

  it('hides user action buttons when session has no button permissions', async () => {
    mockedGetCurrentUserPermissions.mockResolvedValue({
      menuTree: [],
      permissions: [],
    });
    mockedListUsers.mockResolvedValue({
      records: [activeAdminUser],
      total: 1,
      size: 10,
      current: 1,
      pages: 1,
    });

    renderPage({ permissions: [] });

    expect(await screen.findByText('系统管理员')).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: '新建用户' })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: '编辑 admin' })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: '停用 admin' })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: '分配角色 admin' })).not.toBeInTheDocument();
  });

  it('refreshes button permissions when local session is stale', async () => {
    mockedListUsers.mockResolvedValue({
      records: [activeAdminUser],
      total: 1,
      size: 10,
      current: 1,
      pages: 1,
    });

    renderPage({ permissions: [] });

    expect(await screen.findByText('系统管理员')).toBeInTheDocument();
    expect(await screen.findByRole('button', { name: '新建用户' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: '编辑 admin' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: '停用 admin' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: '分配角色 admin' })).toBeInTheDocument();
  });

  it('opens role assignment with current user roles selected', async () => {
    mockedListUsers.mockResolvedValue({
      records: [activeAdminUser],
      total: 1,
      size: 10,
      current: 1,
      pages: 1,
    });
    mockedListRoles.mockResolvedValue({
      records: [
        {
          id: '1',
          roleCode: 'ADMIN',
          roleName: '系统管理员',
          description: '系统管理员角色',
          status: 'ACTIVE',
        },
        {
          id: snowflakeRoleId,
          roleCode: 'PRODUCTION_MANAGER',
          roleName: '生产主管',
          status: 'ACTIVE',
        },
      ],
      total: 2,
      size: 100,
      current: 1,
      pages: 1,
    });

    renderPage();

    expect(await screen.findByText('系统管理员')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: '分配角色 admin' }));

    expect(await screen.findByText('分配角色 - admin')).toBeInTheDocument();
    expect(await screen.findByText('系统管理员（ADMIN）')).toBeInTheDocument();
    await waitFor(() => expect(mockedListRoles).toHaveBeenCalledWith({ current: 1, size: 100 }));
    fireEvent.mouseDown(screen.getByRole('combobox', { name: '角色' }));
    expect(await screen.findByText('生产主管（PRODUCTION_MANAGER）')).toBeInTheDocument();
    expect(mockedListRoles).toHaveBeenCalledWith({ current: 1, size: 100 });
  });

  it('assigns selected roles with string ids and reloads the list', async () => {
    const userWithoutRole = {
      ...activeAdminUser,
      roleIds: [],
    };
    mockedListUsers.mockResolvedValue({
      records: [userWithoutRole],
      total: 1,
      size: 10,
      current: 1,
      pages: 1,
    });
    mockedListRoles.mockResolvedValue({
      records: [
        {
          id: snowflakeRoleId,
          roleCode: 'PRODUCTION_MANAGER',
          roleName: '生产主管',
          status: 'ACTIVE',
        },
      ],
      total: 1,
      size: 100,
      current: 1,
      pages: 1,
    });
    mockedAssignUserRoles.mockResolvedValue(undefined);

    renderPage();

    expect(await screen.findByText('系统管理员')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: '分配角色 admin' }));
    await waitFor(() => expect(mockedListRoles).toHaveBeenCalledWith({ current: 1, size: 100 }));
    fireEvent.mouseDown(screen.getByRole('combobox', { name: '角色' }));
    fireEvent.click(await screen.findByText('生产主管（PRODUCTION_MANAGER）'));
    fireEvent.click(screen.getByRole('button', { name: '保存角色' }));

    await waitFor(() =>
      expect(mockedAssignUserRoles).toHaveBeenCalledWith('1', {
        roleIds: [snowflakeRoleId],
      }),
    );
    expect(mockedListUsers).toHaveBeenCalledTimes(2);
  });
});

function renderPage(sessionOverrides: Partial<AuthSession> = {}) {
  setAuthSession({
    userId: '1',
    username: 'admin',
    realName: '系统管理员',
    roleIds: ['1'],
    permissions: userActionPermissionCodes,
    menuTree: [],
    ...sessionOverrides,
  });

  render(
    <AntdApp>
      <UserManagementPage />
    </AntdApp>,
  );
}
