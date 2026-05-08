import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { App as AntdApp } from 'antd';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { MenuManagementPage } from './MenuManagementPage';
import { getCurrentUserPermissions } from '@/services/auth/authService';
import { createMenu, deleteMenu, listMenus, updateMenu } from '@/services/system/menuService';
import { setAuthSession } from '@/shared/storage/authSessionStorage';

vi.mock('@/services/auth/authService', () => ({
  getCurrentUserPermissions: vi.fn(),
}));

vi.mock('@/services/system/menuService', () => ({
  createMenu: vi.fn(),
  deleteMenu: vi.fn(),
  listMenus: vi.fn(),
  updateMenu: vi.fn(),
}));

const mockedGetCurrentUserPermissions = vi.mocked(getCurrentUserPermissions);
const mockedCreateMenu = vi.mocked(createMenu);
const mockedDeleteMenu = vi.mocked(deleteMenu);
const mockedListMenus = vi.mocked(listMenus);
const mockedUpdateMenu = vi.mocked(updateMenu);
const menuActionPermissionCodes = ['system:menu:create', 'system:menu:update', 'system:menu:delete'];

const systemMenuTree = [
  {
    id: '100',
    parentId: '0',
    name: '系统管理',
    type: 'DIRECTORY',
    path: '/system',
    component: '',
    permission: '',
    icon: 'SettingOutlined',
    sortOrder: 1,
    visible: true,
    status: 'ACTIVE',
    children: [
      {
        id: '130',
        parentId: '100',
        name: '菜单管理',
        type: 'MENU',
        path: '/system/menu',
        component: 'system/MenuList',
        permission: '',
        icon: 'MenuOutlined',
        sortOrder: 3,
        visible: true,
        status: 'ACTIVE',
        children: [],
      },
    ],
  },
];

describe('MenuManagementPage', () => {
  beforeEach(() => {
    window.localStorage.clear();
    mockedGetCurrentUserPermissions.mockReset();
    mockedGetCurrentUserPermissions.mockResolvedValue({
      menuTree: [],
      permissions: menuActionPermissionCodes,
    });
    mockedCreateMenu.mockReset();
    mockedDeleteMenu.mockReset();
    mockedListMenus.mockReset();
    mockedUpdateMenu.mockReset();
  });

  it('loads and renders the menu tree', async () => {
    mockedListMenus.mockResolvedValue(systemMenuTree);

    renderPage();

    expect(screen.getByText('正在加载菜单数据')).toBeInTheDocument();
    expect(await screen.findByText('系统管理')).toBeInTheDocument();
    await waitFor(() => expect(screen.getAllByText('菜单管理').length).toBeGreaterThanOrEqual(2));
    expect(screen.getByText('/system/menu')).toBeInTheDocument();
    expect(screen.getByText('MENU')).toBeInTheDocument();
    expect(mockedListMenus).toHaveBeenCalledTimes(1);
  });

  it('shows empty state when there are no menus', async () => {
    mockedListMenus.mockResolvedValue([]);

    renderPage();

    expect(await screen.findByText('暂无菜单数据')).toBeInTheDocument();
  });

  it('shows backend error message and retries loading', async () => {
    mockedListMenus.mockRejectedValueOnce(new Error('菜单加载失败'));
    mockedListMenus.mockResolvedValueOnce([]);

    renderPage();

    expect(await screen.findByText('菜单加载失败')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: '重新加载' }));

    await waitFor(() => expect(mockedListMenus).toHaveBeenCalledTimes(2));
  });

  it('creates a menu and reloads the tree', async () => {
    mockedListMenus.mockResolvedValue(systemMenuTree);
    mockedCreateMenu.mockResolvedValue(systemMenuTree[0].children[0]);

    renderPage();

    await waitFor(() => expect(screen.getAllByText('菜单管理').length).toBeGreaterThanOrEqual(2));
    fireEvent.click(screen.getByRole('button', { name: '新建菜单' }));
    fireEvent.change(screen.getByLabelText('菜单名称'), { target: { value: ' 报表中心 ' } });
    fireEvent.mouseDown(screen.getByLabelText('菜单类型'));
    fireEvent.click((await screen.findAllByText('菜单')).at(-1)!);
    fireEvent.change(screen.getByLabelText('父菜单ID'), { target: { value: '100' } });
    fireEvent.change(screen.getByLabelText('路由路径'), { target: { value: ' /report ' } });
    fireEvent.change(screen.getByLabelText('前端组件'), { target: { value: ' report/ReportList ' } });
    fireEvent.change(screen.getByLabelText('图标'), { target: { value: ' FileSearchOutlined ' } });
    fireEvent.change(screen.getByLabelText('排序号'), { target: { value: '8' } });
    fireEvent.click(screen.getByRole('button', { name: '保存' }));

    await waitFor(() =>
      expect(mockedCreateMenu).toHaveBeenCalledWith({
        parentId: '100',
        name: '报表中心',
        type: 'MENU',
        path: '/report',
        component: 'report/ReportList',
        icon: 'FileSearchOutlined',
        sortOrder: 8,
        visible: true,
      }),
    );
    expect(mockedListMenus).toHaveBeenCalledTimes(2);
  });

  it('requires permission for button menus', async () => {
    mockedListMenus.mockResolvedValue(systemMenuTree);

    renderPage();

    await waitFor(() => expect(screen.getAllByText('菜单管理').length).toBeGreaterThanOrEqual(2));
    fireEvent.click(screen.getByRole('button', { name: '新建菜单' }));
    fireEvent.change(screen.getByLabelText('菜单名称'), { target: { value: '创建菜单' } });
    fireEvent.mouseDown(screen.getByLabelText('菜单类型'));
    fireEvent.click(await screen.findByText('按钮'));
    fireEvent.click(screen.getByRole('button', { name: '保存' }));

    expect(await screen.findByText('请输入权限标识')).toBeInTheDocument();
    expect(mockedCreateMenu).not.toHaveBeenCalled();
  });

  it('updates a menu and reloads the tree', async () => {
    mockedListMenus.mockResolvedValue(systemMenuTree);
    mockedUpdateMenu.mockResolvedValue({ ...systemMenuTree[0].children[0], name: '菜单配置' });

    renderPage();

    await waitFor(() => expect(screen.getAllByText('菜单管理').length).toBeGreaterThanOrEqual(2));
    fireEvent.click(screen.getByRole('button', { name: '编辑 菜单管理' }));
    fireEvent.change(screen.getByLabelText('菜单名称'), { target: { value: ' 菜单配置 ' } });
    fireEvent.change(screen.getByLabelText('排序号'), { target: { value: '4' } });
    fireEvent.mouseDown(screen.getByLabelText('状态'));
    fireEvent.click(await screen.findByText('停用'));
    fireEvent.click(screen.getByRole('button', { name: '保存' }));

    await waitFor(() =>
      expect(mockedUpdateMenu).toHaveBeenCalledWith('130', {
        parentId: '100',
        name: '菜单配置',
        type: 'MENU',
        path: '/system/menu',
        component: 'system/MenuList',
        icon: 'MenuOutlined',
        sortOrder: 4,
        visible: true,
        status: 'INACTIVE',
      }),
    );
    expect(mockedListMenus).toHaveBeenCalledTimes(2);
  });

  it('deletes a menu after confirmation and reloads the tree', async () => {
    mockedListMenus.mockResolvedValue(systemMenuTree);
    mockedDeleteMenu.mockResolvedValue(undefined);

    renderPage();

    await waitFor(() => expect(screen.getAllByText('菜单管理').length).toBeGreaterThanOrEqual(2));
    fireEvent.click(screen.getByRole('button', { name: '删除 菜单管理' }));
    fireEvent.click(screen.getByRole('button', { name: '确认删除' }));

    await waitFor(() => expect(mockedDeleteMenu).toHaveBeenCalledWith('130'));
    expect(mockedListMenus).toHaveBeenCalledTimes(2);
  });

  it('hides menu actions without permissions', async () => {
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
    mockedListMenus.mockResolvedValue(systemMenuTree);

    renderPage();

    await waitFor(() => expect(screen.getAllByText('菜单管理').length).toBeGreaterThanOrEqual(2));
    expect(screen.queryByRole('button', { name: '新建菜单' })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: '编辑 菜单管理' })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: '删除 菜单管理' })).not.toBeInTheDocument();
  });
});

function renderPage() {
  render(
    <AntdApp>
      <MenuManagementPage />
    </AntdApp>,
  );
}
