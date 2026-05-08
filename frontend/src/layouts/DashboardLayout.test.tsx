import { fireEvent, render, screen } from '@testing-library/react';
import { App as AntdApp } from 'antd';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { beforeEach, describe, expect, it } from 'vitest';
import { DashboardLayout } from './DashboardLayout';
import { clearAuthSession, setAuthSession } from '@/shared/storage/authSessionStorage';

describe('DashboardLayout', () => {
  beforeEach(() => {
    window.localStorage.clear();
    clearAuthSession();
  });

  it('renders backend menu tree and current user name from auth session', async () => {
    setAuthSession({
      userId: '1',
      username: 'admin',
      realName: '系统管理员',
      roleIds: ['1'],
      permissions: ['system:user:create'],
      menuTree: [
        {
          id: '10',
          parentId: '0',
          name: '系统管理',
          type: 'DIRECTORY',
          path: '/system',
          icon: 'SettingOutlined',
          sortOrder: 1,
          visible: true,
          status: 'ACTIVE',
          children: [
            {
              id: '11',
              parentId: '10',
              name: '用户管理',
              type: 'MENU',
              path: '/system/users',
              icon: 'TeamOutlined',
              sortOrder: 1,
              visible: true,
              status: 'ACTIVE',
              children: [],
            },
          ],
        },
      ],
    });

    render(
      <AntdApp>
        <MemoryRouter
          initialEntries={['/']}
          future={{ v7_relativeSplatPath: true, v7_startTransition: true }}
        >
          <Routes>
            <Route path="/" element={<DashboardLayout />}>
              <Route index element={<div>工作区内容</div>} />
            </Route>
          </Routes>
        </MemoryRouter>
      </AntdApp>,
    );

    fireEvent.click(await screen.findByText('系统管理'));

    expect(await screen.findByText('用户管理')).toBeInTheDocument();
    expect(screen.getByText('系统管理员')).toBeInTheDocument();
    expect(screen.getByText('工作区内容')).toBeInTheDocument();
  });

  it('normalizes backend user menu path to the frontend route', async () => {
    setAuthSession({
      userId: '1',
      username: 'admin',
      realName: '系统管理员',
      roleIds: ['1'],
      permissions: ['system:user:create'],
      menuTree: [
        {
          id: '10',
          parentId: '0',
          name: '系统管理',
          type: 'DIRECTORY',
          path: '/system',
          icon: 'SettingOutlined',
          sortOrder: 1,
          visible: true,
          status: 'ACTIVE',
          children: [
            {
              id: '11',
              parentId: '10',
              name: '用户管理',
              type: 'MENU',
              path: '/system/user',
              icon: 'TeamOutlined',
              sortOrder: 1,
              visible: true,
              status: 'ACTIVE',
              children: [],
            },
          ],
        },
      ],
    });

    renderLayout('/');

    fireEvent.click(await screen.findByText('系统管理'));
    fireEvent.click(await screen.findByText('用户管理'));

    expect(await screen.findByText('用户管理页面')).toBeInTheDocument();
  });

  it('shows the system user menu from fallback when backend menu tree is empty', async () => {
    setAuthSession({
      userId: '1',
      username: 'admin',
      realName: '系统管理员',
      roleIds: ['1'],
      permissions: ['system:user:assignRole'],
      menuTree: [],
    });

    renderLayout('/system/users');

    fireEvent.click(await screen.findByText('系统管理'));

    expect(await screen.findAllByText('用户管理')).toHaveLength(2);
    expect(screen.getByText('用户管理页面')).toBeInTheDocument();
  });

  it('normalizes backend role menu path to the frontend route', async () => {
    setAuthSession({
      userId: '1',
      username: 'admin',
      realName: '系统管理员',
      roleIds: ['1'],
      permissions: ['system:role:update'],
      menuTree: [
        {
          id: '10',
          parentId: '0',
          name: '系统管理',
          type: 'DIRECTORY',
          path: '/system',
          icon: 'SettingOutlined',
          sortOrder: 1,
          visible: true,
          status: 'ACTIVE',
          children: [
            {
              id: '12',
              parentId: '10',
              name: '角色管理',
              type: 'MENU',
              path: '/system/role',
              icon: 'TeamOutlined',
              sortOrder: 2,
              visible: true,
              status: 'ACTIVE',
              children: [],
            },
          ],
        },
      ],
    });

    renderLayout('/');

    fireEvent.click(await screen.findByText('系统管理'));
    fireEvent.click(await screen.findByText('角色管理'));

    expect(await screen.findByText('角色管理页面')).toBeInTheDocument();
  });

  it('shows the system role menu from fallback when backend menu tree is empty', async () => {
    setAuthSession({
      userId: '1',
      username: 'admin',
      realName: '系统管理员',
      roleIds: ['1'],
      permissions: ['system:role:update'],
      menuTree: [],
    });

    renderLayout('/system/roles');

    fireEvent.click(await screen.findByText('系统管理'));

    expect(await screen.findAllByText('角色管理')).toHaveLength(2);
    expect(screen.getByText('角色管理页面')).toBeInTheDocument();
  });

  it('normalizes backend menu management path to the frontend route', async () => {
    setAuthSession({
      userId: '1',
      username: 'admin',
      realName: '系统管理员',
      roleIds: ['1'],
      permissions: ['system:menu:update'],
      menuTree: [
        {
          id: '10',
          parentId: '0',
          name: '系统管理',
          type: 'DIRECTORY',
          path: '/system',
          icon: 'SettingOutlined',
          sortOrder: 1,
          visible: true,
          status: 'ACTIVE',
          children: [
            {
              id: '13',
              parentId: '10',
              name: '菜单管理',
              type: 'MENU',
              path: '/system/menu',
              icon: 'MenuOutlined',
              sortOrder: 3,
              visible: true,
              status: 'ACTIVE',
              children: [],
            },
          ],
        },
      ],
    });

    renderLayout('/');

    fireEvent.click(await screen.findByText('系统管理'));
    fireEvent.click(await screen.findByText('菜单管理'));

    expect(await screen.findByText('菜单管理页面')).toBeInTheDocument();
  });

  it('shows the system menu management item from fallback when backend menu tree is empty', async () => {
    setAuthSession({
      userId: '1',
      username: 'admin',
      realName: '系统管理员',
      roleIds: ['1'],
      permissions: ['system:menu:update'],
      menuTree: [],
    });

    renderLayout('/system/menus');

    fireEvent.click(await screen.findByText('系统管理'));

    expect(await screen.findAllByText('菜单管理')).toHaveLength(2);
    expect(screen.getByText('菜单管理页面')).toBeInTheDocument();
  });

  it('normalizes backend config menu path to the frontend route', async () => {
    setAuthSession({
      userId: '1',
      username: 'admin',
      realName: '系统管理员',
      roleIds: ['1'],
      permissions: ['system:config:update'],
      menuTree: [
        {
          id: '10',
          parentId: '0',
          name: '系统管理',
          type: 'DIRECTORY',
          path: '/system',
          icon: 'SettingOutlined',
          sortOrder: 1,
          visible: true,
          status: 'ACTIVE',
          children: [
            {
              id: '16',
              parentId: '10',
              name: '系统配置',
              type: 'MENU',
              path: '/system/config',
              icon: 'ToolOutlined',
              sortOrder: 6,
              visible: true,
              status: 'ACTIVE',
              children: [],
            },
          ],
        },
      ],
    });

    renderLayout('/');

    fireEvent.click(await screen.findByText('系统管理'));
    fireEvent.click(await screen.findByText('系统配置'));

    expect(await screen.findByText('系统配置页面')).toBeInTheDocument();
  });

  it('shows the system config item from fallback when backend menu tree is empty', async () => {
    setAuthSession({
      userId: '1',
      username: 'admin',
      realName: '系统管理员',
      roleIds: ['1'],
      permissions: ['system:config:update'],
      menuTree: [],
    });

    renderLayout('/system/configs');

    fireEvent.click(await screen.findByText('系统管理'));

    expect(await screen.findAllByText('系统配置')).toHaveLength(2);
    expect(screen.getByText('系统配置页面')).toBeInTheDocument();
  });

  it('normalizes backend customer and supplier menu paths to frontend routes', async () => {
    setAuthSession({
      userId: '1',
      username: 'admin',
      realName: '系统管理员',
      roleIds: ['1'],
      permissions: ['master:customer:create', 'master:supplier:create'],
      menuTree: [
        {
          id: '200',
          parentId: '0',
          name: '基础数据',
          type: 'DIRECTORY',
          path: '/master',
          icon: 'DatabaseOutlined',
          sortOrder: 2,
          visible: true,
          status: 'ACTIVE',
          children: [
            {
              id: '230',
              parentId: '200',
              name: '供应商管理',
              type: 'MENU',
              path: '/master/supplier',
              icon: 'SolutionOutlined',
              sortOrder: 3,
              visible: true,
              status: 'ACTIVE',
              children: [],
            },
            {
              id: '240',
              parentId: '200',
              name: '客户管理',
              type: 'MENU',
              path: '/master/customer',
              icon: 'SmileOutlined',
              sortOrder: 4,
              visible: true,
              status: 'ACTIVE',
              children: [],
            },
          ],
        },
      ],
    });

    renderLayout('/');

    fireEvent.click(await screen.findByText('基础数据'));
    fireEvent.click(await screen.findByText('客户管理'));
    expect(await screen.findByText('客户管理页面')).toBeInTheDocument();
    fireEvent.click(await screen.findByText('供应商管理'));
    expect(await screen.findByText('供应商管理页面')).toBeInTheDocument();
  });

  it('shows customer and supplier menus from fallback when backend menu tree is empty', async () => {
    setAuthSession({
      userId: '1',
      username: 'admin',
      realName: '系统管理员',
      roleIds: ['1'],
      permissions: ['master:customer:create', 'master:supplier:create'],
      menuTree: [],
    });

    renderLayout('/master/customers');

    fireEvent.click(await screen.findByText('基础数据'));

    expect(await screen.findAllByText('客户管理')).toHaveLength(2);
    expect(screen.getByText('供应商管理')).toBeInTheDocument();
    expect(screen.getByText('客户管理页面')).toBeInTheDocument();
  });

  it('normalizes backend material and category menu paths to frontend routes', async () => {
    setAuthSession({
      userId: '1',
      username: 'admin',
      realName: '系统管理员',
      roleIds: ['1'],
      permissions: ['master:material:create', 'master:category:create'],
      menuTree: [
        {
          id: '200',
          parentId: '0',
          name: '基础数据',
          type: 'DIRECTORY',
          path: '/master',
          icon: 'DatabaseOutlined',
          sortOrder: 2,
          visible: true,
          status: 'ACTIVE',
          children: [
            {
              id: '210',
              parentId: '200',
              name: '物料管理',
              type: 'MENU',
              path: '/master/material',
              icon: 'BoxOutlined',
              sortOrder: 1,
              visible: true,
              status: 'ACTIVE',
              children: [],
            },
            {
              id: '270',
              parentId: '200',
              name: '物料分类',
              type: 'MENU',
              path: '/master/category',
              icon: 'ApartmentOutlined',
              sortOrder: 7,
              visible: true,
              status: 'ACTIVE',
              children: [],
            },
          ],
        },
      ],
    });

    renderLayout('/');

    fireEvent.click(await screen.findByText('基础数据'));
    fireEvent.click(await screen.findByText('物料管理'));
    expect(await screen.findByText('物料管理页面')).toBeInTheDocument();
    fireEvent.click(await screen.findByText('物料分类'));
    expect(await screen.findByText('物料分类页面')).toBeInTheDocument();
  });

  it('shows material and category menus from fallback when backend menu tree is empty', async () => {
    setAuthSession({
      userId: '1',
      username: 'admin',
      realName: '系统管理员',
      roleIds: ['1'],
      permissions: ['master:material:create', 'master:category:create'],
      menuTree: [],
    });

    renderLayout('/master/materials');

    fireEvent.click(await screen.findByText('基础数据'));

    expect(await screen.findAllByText('物料管理')).toHaveLength(2);
    expect(screen.getByText('物料分类')).toBeInTheDocument();
    expect(screen.getByText('物料管理页面')).toBeInTheDocument();
  });

  it('normalizes backend SPU and size group menu paths to frontend routes', async () => {
    setAuthSession({
      userId: '1',
      username: 'admin',
      realName: '系统管理员',
      roleIds: ['1'],
      permissions: ['master:spu:create', 'master:sizeGroup:create'],
      menuTree: [
        {
          id: '200',
          parentId: '0',
          name: '基础数据',
          type: 'DIRECTORY',
          path: '/master',
          icon: 'DatabaseOutlined',
          sortOrder: 2,
          visible: true,
          status: 'ACTIVE',
          children: [
            {
              id: '220',
              parentId: '200',
              name: '款式管理',
              type: 'MENU',
              path: '/master/spu',
              icon: 'SkinOutlined',
              sortOrder: 2,
              visible: true,
              status: 'ACTIVE',
              children: [],
            },
            {
              id: '280',
              parentId: '200',
              name: '尺码组管理',
              type: 'MENU',
              path: '/master/sizeGroup',
              icon: 'ColumnWidthOutlined',
              sortOrder: 8,
              visible: true,
              status: 'ACTIVE',
              children: [],
            },
          ],
        },
      ],
    });

    renderLayout('/');

    fireEvent.click(await screen.findByText('基础数据'));
    fireEvent.click(await screen.findByText('款式管理'));
    expect(await screen.findByText('款式管理页面')).toBeInTheDocument();
    fireEvent.click(await screen.findByText('尺码组管理'));
    expect(await screen.findByText('尺码组管理页面')).toBeInTheDocument();
  });

  it('shows SPU and size group menus from fallback when backend menu tree is empty', async () => {
    setAuthSession({
      userId: '1',
      username: 'admin',
      realName: '系统管理员',
      roleIds: ['1'],
      permissions: ['master:spu:create', 'master:sizeGroup:create'],
      menuTree: [],
    });

    renderLayout('/master/spus');

    fireEvent.click(await screen.findByText('基础数据'));

    expect(await screen.findAllByText('款式管理')).toHaveLength(2);
    expect(screen.getByText('尺码组管理')).toBeInTheDocument();
    expect(screen.getByText('款式管理页面')).toBeInTheDocument();
  });
});

function renderLayout(initialPath: string) {
  render(
    <AntdApp>
      <MemoryRouter
        initialEntries={[initialPath]}
        future={{ v7_relativeSplatPath: true, v7_startTransition: true }}
      >
        <Routes>
          <Route path="/" element={<DashboardLayout />}>
            <Route index element={<div>工作区内容</div>} />
            <Route path="system/users" element={<div>用户管理页面</div>} />
            <Route path="system/roles" element={<div>角色管理页面</div>} />
            <Route path="system/menus" element={<div>菜单管理页面</div>} />
            <Route path="system/configs" element={<div>系统配置页面</div>} />
            <Route path="master/customers" element={<div>客户管理页面</div>} />
            <Route path="master/suppliers" element={<div>供应商管理页面</div>} />
            <Route path="master/materials" element={<div>物料管理页面</div>} />
            <Route path="master/categories" element={<div>物料分类页面</div>} />
            <Route path="master/spus" element={<div>款式管理页面</div>} />
            <Route path="master/size-groups" element={<div>尺码组管理页面</div>} />
          </Route>
        </Routes>
      </MemoryRouter>
    </AntdApp>,
  );
}
