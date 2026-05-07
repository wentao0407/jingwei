import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { App as AntdApp } from 'antd';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { RoleManagementPage } from './RoleManagementPage';
import { listRoles } from '@/services/system/roleService';

vi.mock('@/services/system/roleService', () => ({
  listRoles: vi.fn(),
}));

const mockedListRoles = vi.mocked(listRoles);

const activeRole = {
  id: '2051932034979037191',
  roleCode: 'WAREHOUSE_MANAGER',
  roleName: '仓库主管',
  description: '管理仓储入库、出库与库存盘点',
  status: 'ACTIVE',
  createdAt: '2026-05-06T10:00:00',
  updatedAt: '2026-05-06T11:00:00',
};

describe('RoleManagementPage', () => {
  beforeEach(() => {
    mockedListRoles.mockReset();
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
});

function renderPage() {
  render(
    <AntdApp>
      <RoleManagementPage />
    </AntdApp>,
  );
}
