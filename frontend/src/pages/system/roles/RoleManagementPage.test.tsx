import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { App as AntdApp } from 'antd';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { RoleManagementPage } from './RoleManagementPage';
import { getCurrentUserPermissions } from '@/services/auth/authService';
import { assignMenuPermissions, getRoleMenuIds, listMenus } from '@/services/system/menuService';
import { createRole, listRoles, updateRole } from '@/services/system/roleService';
import { setAuthSession } from '@/shared/storage/authSessionStorage';

vi.mock('@/services/auth/authService', () => ({
  getCurrentUserPermissions: vi.fn(),
}));

vi.mock('@/services/system/roleService', () => ({
  createRole: vi.fn(),
  listRoles: vi.fn(),
  updateRole: vi.fn(),
}));

vi.mock('@/services/system/menuService', () => ({
  assignMenuPermissions: vi.fn(),
  getRoleMenuIds: vi.fn(),
  listMenus: vi.fn(),
}));

const mockedGetCurrentUserPermissions = vi.mocked(getCurrentUserPermissions);
const mockedAssignMenuPermissions = vi.mocked(assignMenuPermissions);
const mockedCreateRole = vi.mocked(createRole);
const mockedGetRoleMenuIds = vi.mocked(getRoleMenuIds);
const mockedListMenus = vi.mocked(listMenus);
const mockedListRoles = vi.mocked(listRoles);
const mockedUpdateRole = vi.mocked(updateRole);
const roleActionPermissionCodes = ['system:role:create', 'system:role:update', 'system:role:assignPermission'];

const activeRole = {
  id: '2051932034979037191',
  roleCode: 'WAREHOUSE_MANAGER',
  roleName: '仓库主管',
  description: '管理仓储入库、出库与库存盘点',
  status: 'ACTIVE',
  createdAt: '2026-05-06T10:00:00',
  updatedAt: '2026-05-06T11:00:00',
};

const systemMenuTree = [
  {
    id: '100',
    parentId: '0',
    name: '系统管理',
    type: 'DIRECTORY',
    path: '/system',
    sortOrder: 1,
    visible: true,
    status: 'ACTIVE',
    children: [
      {
        id: '120',
        parentId: '100',
        name: '角色管理',
        type: 'MENU',
        path: '/system/role',
        sortOrder: 2,
        visible: true,
        status: 'ACTIVE',
        children: [
          {
            id: '121',
            parentId: '120',
            name: '创建角色',
            type: 'BUTTON',
            permission: 'system:role:create',
            sortOrder: 1,
            visible: true,
            status: 'ACTIVE',
            children: [],
          },
        ],
      },
    ],
  },
];

describe('RoleManagementPage', () => {
  beforeEach(() => {
    window.localStorage.clear();
    mockedGetCurrentUserPermissions.mockReset();
    mockedGetCurrentUserPermissions.mockResolvedValue({
      menuTree: [],
      permissions: roleActionPermissionCodes,
    });
    mockedAssignMenuPermissions.mockReset();
    mockedCreateRole.mockReset();
    mockedGetRoleMenuIds.mockReset();
    mockedListMenus.mockReset();
    mockedListRoles.mockReset();
    mockedUpdateRole.mockReset();
  });

  it('loads and renders paged roles', async () => {
    mockedListRoles.mockResolvedValue({
      records: [activeRole],
      total: 1,
      size: 10,
      current: 1,
      pages: 1,
    });

    renderPage();

    expect(screen.getByText('正在加载角色数据')).toBeInTheDocument();
    expect(await screen.findByText('仓库主管')).toBeInTheDocument();
    expect(screen.getByText('WAREHOUSE_MANAGER')).toBeInTheDocument();
    expect(screen.getByText('管理仓储入库、出库与库存盘点')).toBeInTheDocument();
    expect(screen.getByText('启用')).toBeInTheDocument();
    expect(mockedListRoles).toHaveBeenCalledWith({ current: 1, size: 10 });
  });

  it('shows empty state when there are no roles', async () => {
    mockedListRoles.mockResolvedValue({
      records: [],
      total: 0,
      size: 10,
      current: 1,
      pages: 0,
    });

    renderPage();

    expect(await screen.findByText('暂无角色数据')).toBeInTheDocument();
  });

  it('shows backend error message and retries loading', async () => {
    mockedListRoles.mockRejectedValueOnce(new Error('角色加载失败'));
    mockedListRoles.mockResolvedValueOnce({
      records: [],
      total: 0,
      size: 10,
      current: 1,
      pages: 0,
    });

    renderPage();

    expect(await screen.findByText('角色加载失败')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: '重新加载' }));

    await waitFor(() => expect(mockedListRoles).toHaveBeenCalledTimes(2));
  });

  it('searches roles by keyword', async () => {
    mockedListRoles.mockResolvedValue({
      records: [],
      total: 0,
      size: 10,
      current: 1,
      pages: 0,
    });

    renderPage();

    await screen.findByText('暂无角色数据');
    fireEvent.change(screen.getByPlaceholderText('搜索角色编码/角色名称'), {
      target: { value: ' WAREHOUSE ' },
    });
    fireEvent.click(screen.getByRole('button', { name: '查询' }));

    await waitFor(() =>
      expect(mockedListRoles).toHaveBeenLastCalledWith({ current: 1, size: 10, keyword: 'WAREHOUSE' }),
    );
  });

  it('filters roles by status', async () => {
    mockedListRoles.mockResolvedValue({
      records: [],
      total: 0,
      size: 10,
      current: 1,
      pages: 0,
    });

    renderPage();

    await screen.findByText('暂无角色数据');
    fireEvent.mouseDown(screen.getByText('全部状态'));
    fireEvent.click(await screen.findByText('停用'));

    await waitFor(() =>
      expect(mockedListRoles).toHaveBeenLastCalledWith({ current: 1, size: 10, status: 'INACTIVE' }),
    );
  });

  it('creates a role and reloads the list', async () => {
    mockedListRoles.mockResolvedValue({
      records: [],
      total: 0,
      size: 10,
      current: 1,
      pages: 0,
    });
    mockedCreateRole.mockResolvedValue(activeRole);

    renderPage();

    await screen.findByText('暂无角色数据');
    fireEvent.click(screen.getByRole('button', { name: '新建角色' }));
    fireEvent.change(screen.getByLabelText('角色编码'), { target: { value: ' QUALITY_MANAGER ' } });
    fireEvent.change(screen.getByLabelText('角色名称'), { target: { value: ' 质检主管 ' } });
    fireEvent.change(screen.getByLabelText('角色描述'), { target: { value: ' 负责质检流程 ' } });
    fireEvent.click(screen.getByRole('button', { name: '保存' }));

    await waitFor(() =>
      expect(mockedCreateRole).toHaveBeenCalledWith({
        roleCode: 'QUALITY_MANAGER',
        roleName: '质检主管',
        description: '负责质检流程',
      }),
    );
    expect(mockedListRoles).toHaveBeenCalledTimes(2);
  });

  it('blocks creating roles with invalid input', async () => {
    mockedListRoles.mockResolvedValue({
      records: [],
      total: 0,
      size: 10,
      current: 1,
      pages: 0,
    });

    renderPage();

    await screen.findByText('暂无角色数据');
    fireEvent.click(screen.getByRole('button', { name: '新建角色' }));
    fireEvent.change(screen.getByLabelText('角色编码'), { target: { value: 'bad role' } });
    fireEvent.click(screen.getByRole('button', { name: '保存' }));

    expect(await screen.findByText('角色编码只能包含大写字母、数字和下划线')).toBeInTheDocument();
    expect(screen.getByText('请输入角色名称')).toBeInTheDocument();
    expect(mockedCreateRole).not.toHaveBeenCalled();
  });

  it('updates a role and reloads the list', async () => {
    mockedListRoles.mockResolvedValue({
      records: [activeRole],
      total: 1,
      size: 10,
      current: 1,
      pages: 1,
    });
    mockedUpdateRole.mockResolvedValue({ ...activeRole, roleName: '仓储主管', status: 'INACTIVE' });

    renderPage();

    expect(await screen.findByText('仓库主管')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: '编辑 WAREHOUSE_MANAGER' }));
    fireEvent.change(screen.getByLabelText('角色名称'), { target: { value: ' 仓储主管 ' } });
    fireEvent.change(screen.getByLabelText('角色描述'), { target: { value: ' 管理仓储流程 ' } });
    fireEvent.mouseDown(screen.getByLabelText('状态'));
    fireEvent.click(await screen.findByText('停用'));
    fireEvent.click(screen.getByRole('button', { name: '保存' }));

    await waitFor(() =>
      expect(mockedUpdateRole).toHaveBeenCalledWith('2051932034979037191', {
        roleName: '仓储主管',
        description: '管理仓储流程',
        status: 'INACTIVE',
      }),
    );
    expect(mockedListRoles).toHaveBeenCalledTimes(2);
  });

  it('assigns menu permissions to a role', async () => {
    mockedListRoles.mockResolvedValue({
      records: [activeRole],
      total: 1,
      size: 10,
      current: 1,
      pages: 1,
    });
    mockedListMenus.mockResolvedValue(systemMenuTree);
    mockedGetRoleMenuIds.mockResolvedValue(['100', '120', '121']);
    mockedAssignMenuPermissions.mockResolvedValue(undefined);

    renderPage();

    expect(await screen.findByText('仓库主管')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: '分配权限 WAREHOUSE_MANAGER' }));

    expect(await screen.findByText('为 WAREHOUSE_MANAGER 分配菜单和按钮权限')).toBeInTheDocument();
    expect(screen.getByText('系统管理')).toBeInTheDocument();
    expect(screen.getAllByText('角色管理').length).toBeGreaterThanOrEqual(2);
    expect(screen.getByText('创建角色')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: '保存权限' }));

    await waitFor(() =>
      expect(mockedAssignMenuPermissions).toHaveBeenCalledWith({
        roleId: '2051932034979037191',
        menuIds: ['100', '120', '121'],
      }),
    );
  });

  it('hides role actions without permissions', async () => {
    mockedGetCurrentUserPermissions.mockResolvedValue({
      menuTree: [],
      permissions: [],
    });
    setAuthSession({
      userId: '1',
      username: 'viewer',
      realName: '只读用户',
      roleIds: [],
      permissions: [],
      menuTree: [],
    });
    mockedListRoles.mockResolvedValue({
      records: [activeRole],
      total: 1,
      size: 10,
      current: 1,
      pages: 1,
    });

    renderPage();

    expect(await screen.findByText('仓库主管')).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: '新建角色' })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: '编辑 WAREHOUSE_MANAGER' })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: '分配权限 WAREHOUSE_MANAGER' })).not.toBeInTheDocument();
  });
});

function renderPage() {
  render(
    <AntdApp>
      <RoleManagementPage />
    </AntdApp>,
  );
}
