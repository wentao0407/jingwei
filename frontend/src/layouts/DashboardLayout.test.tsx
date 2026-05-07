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
          </Route>
        </Routes>
      </MemoryRouter>
    </AntdApp>,
  );
}
