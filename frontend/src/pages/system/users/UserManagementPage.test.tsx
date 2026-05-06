import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { App as AntdApp } from 'antd';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { UserManagementPage } from './UserManagementPage';
import { getCurrentUserPermissions } from '@/services/auth/authService';
import { createUser, deactivateUser, listUsers, updateUser } from '@/services/system/userService';
import { setAuthSession, type AuthSession } from '@/shared/storage/authSessionStorage';

vi.mock('@/services/auth/authService', () => ({
  getCurrentUserPermissions: vi.fn(),
}));

vi.mock('@/services/system/userService', () => ({
  createUser: vi.fn(),
  deactivateUser: vi.fn(),
  listUsers: vi.fn(),
  updateUser: vi.fn(),
}));

const mockedGetCurrentUserPermissions = vi.mocked(getCurrentUserPermissions);
const mockedCreateUser = vi.mocked(createUser);
const mockedDeactivateUser = vi.mocked(deactivateUser);
const mockedListUsers = vi.mocked(listUsers);
const mockedUpdateUser = vi.mocked(updateUser);
const userActionPermissionCodes = ['system:user:create', 'system:user:update', 'system:user:deactivate'];

const activeAdminUser = {
  id: 1,
  username: 'admin',
  realName: '系统管理员',
  phone: '13800000000',
  email: 'admin@example.com',
  status: 'ACTIVE',
  roleIds: [1],
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
    mockedCreateUser.mockReset();
    mockedDeactivateUser.mockReset();
    mockedListUsers.mockReset();
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
    mockedCreateUser.mockResolvedValue({ ...activeAdminUser, id: 2, username: 'newuser' });

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
      expect(mockedUpdateUser).toHaveBeenCalledWith(1, {
        realName: '管理员',
        phone: '13800000000',
        email: 'admin@example.com',
        status: 'ACTIVE',
      }),
    );
    expect(mockedListUsers).toHaveBeenCalledTimes(2);
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

    await waitFor(() => expect(mockedDeactivateUser).toHaveBeenCalledWith(1));
    expect(mockedListUsers).toHaveBeenCalledTimes(2);
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
  });
});

function renderPage(sessionOverrides: Partial<AuthSession> = {}) {
  setAuthSession({
    userId: 1,
    username: 'admin',
    realName: '系统管理员',
    roleIds: [1],
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
