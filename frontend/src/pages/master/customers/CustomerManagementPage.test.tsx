import { fireEvent, render, screen, waitFor, within } from '@testing-library/react';
import { App as AntdApp } from 'antd';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { CustomerManagementPage } from './CustomerManagementPage';
import { getCurrentUserPermissions } from '@/services/auth/authService';
import { activateCustomer, createCustomer, deactivateCustomer, deleteCustomer, listCustomers, updateCustomer } from '@/services/master/customerService';
import { setAuthSession } from '@/shared/storage/authSessionStorage';

vi.mock('@/services/auth/authService', () => ({
  getCurrentUserPermissions: vi.fn(),
}));

vi.mock('@/services/master/customerService', () => ({
  activateCustomer: vi.fn(),
  createCustomer: vi.fn(),
  deactivateCustomer: vi.fn(),
  deleteCustomer: vi.fn(),
  listCustomers: vi.fn(),
  updateCustomer: vi.fn(),
}));

const mockedGetCurrentUserPermissions = vi.mocked(getCurrentUserPermissions);
const mockedActivateCustomer = vi.mocked(activateCustomer);
const mockedCreateCustomer = vi.mocked(createCustomer);
const mockedDeactivateCustomer = vi.mocked(deactivateCustomer);
const mockedDeleteCustomer = vi.mocked(deleteCustomer);
const mockedListCustomers = vi.mocked(listCustomers);
const mockedUpdateCustomer = vi.mocked(updateCustomer);
const customerPermissions = [
  'master:customer:create',
  'master:customer:update',
  'master:customer:activate',
  'master:customer:deactivate',
  'master:customer:delete',
];

const customerPage = {
  records: [
    {
      id: '2051932034979037191',
      code: 'CUS-000001',
      name: '杭州云织',
      shortName: '云织',
      type: 'WHOLESALE',
      level: 'A',
      contactPerson: '王经理',
      contactPhone: '13800138000',
      settlementType: 'MONTHLY',
      creditLimit: 500000,
      status: 'ACTIVE',
      updatedAt: '2026-05-07T10:00:00',
    },
    {
      id: '2051932034979037192',
      code: 'CUS-000002',
      name: '上海锦棉',
      type: 'RETAIL',
      level: 'B',
      status: 'INACTIVE',
    },
  ],
  total: 2,
  current: 1,
  size: 10,
  pages: 1,
};

describe('CustomerManagementPage', () => {
  beforeEach(() => {
    window.localStorage.clear();
    mockedGetCurrentUserPermissions.mockReset();
    mockedGetCurrentUserPermissions.mockResolvedValue({ menuTree: [], permissions: customerPermissions });
    mockedActivateCustomer.mockReset();
    mockedCreateCustomer.mockReset();
    mockedDeactivateCustomer.mockReset();
    mockedDeleteCustomer.mockReset();
    mockedListCustomers.mockReset();
    mockedUpdateCustomer.mockReset();
  });

  it('loads and renders paged customers', async () => {
    mockedListCustomers.mockResolvedValue(customerPage);

    renderPage();

    expect(screen.getByText('正在加载客户数据')).toBeInTheDocument();
    expect(await screen.findByText('杭州云织')).toBeInTheDocument();
    expect(screen.getByText('CUS-000001')).toBeInTheDocument();
    expect(screen.getByText('批发客户')).toBeInTheDocument();
    expect(screen.getByText('A级')).toBeInTheDocument();
  });

  it('searches and filters customers', async () => {
    mockedListCustomers.mockResolvedValue(customerPage);

    renderPage();

    await screen.findByText('杭州云织');
    fireEvent.change(screen.getByPlaceholderText('搜索客户编码/名称'), { target: { value: ' 云织 ' } });
    fireEvent.click(screen.getByRole('button', { name: /查询/ }));

    await waitFor(() =>
      expect(mockedListCustomers).toHaveBeenLastCalledWith({
        current: 1,
        size: 10,
        keyword: '云织',
      }),
    );
  });

  it('creates and updates customers', async () => {
    mockedListCustomers.mockResolvedValue(customerPage);
    mockedCreateCustomer.mockResolvedValue(customerPage.records[0]);
    mockedUpdateCustomer.mockResolvedValue(customerPage.records[0]);

    renderPage();

    await screen.findByText('杭州云织');
    fireEvent.click(screen.getByRole('button', { name: '新建客户' }));
    fireEvent.change(screen.getByLabelText('客户名称'), { target: { value: ' 宁波海岚 ' } });
    openDialogSelect('客户类型');
    await chooseOption('线上客户');
    fireEvent.change(screen.getByLabelText('联系电话'), { target: { value: '13900139000' } });
    fireEvent.click(screen.getByRole('button', { name: '保存' }));

    await waitFor(() =>
      expect(mockedCreateCustomer).toHaveBeenCalledWith({
        name: '宁波海岚',
        type: 'ONLINE',
        contactPhone: '13900139000',
      }),
    );

    fireEvent.click(screen.getByRole('button', { name: '编辑 杭州云织' }));
    fireEvent.change(screen.getByLabelText('客户名称'), { target: { value: ' 杭州云织更新 ' } });
    fireEvent.click(screen.getByRole('button', { name: '保存' }));

    await waitFor(() =>
      expect(mockedUpdateCustomer).toHaveBeenCalledWith('2051932034979037191', expect.objectContaining({ name: '杭州云织更新' })),
    );
  });

  it('validates phone before saving customers', async () => {
    mockedListCustomers.mockResolvedValue(customerPage);

    renderPage();

    await screen.findByText('杭州云织');
    fireEvent.click(screen.getByRole('button', { name: '新建客户' }));
    fireEvent.change(screen.getByLabelText('客户名称'), { target: { value: '测试客户' } });
    openDialogSelect('客户类型');
    await chooseOption('批发客户');
    fireEvent.change(screen.getByLabelText('联系电话'), { target: { value: '123' } });
    fireEvent.click(screen.getByRole('button', { name: '保存' }));

    expect(await screen.findByText('请输入正确的手机号')).toBeInTheDocument();
    expect(mockedCreateCustomer).not.toHaveBeenCalled();
  });

  it('activates, deactivates and deletes customers', async () => {
    mockedListCustomers.mockResolvedValue(customerPage);
    mockedActivateCustomer.mockResolvedValue(undefined);
    mockedDeactivateCustomer.mockResolvedValue(undefined);
    mockedDeleteCustomer.mockResolvedValue(undefined);

    renderPage();

    await screen.findByText('杭州云织');
    fireEvent.click(screen.getByRole('button', { name: '停用 杭州云织' }));
    fireEvent.click(screen.getByRole('button', { name: '确认停用' }));
    fireEvent.click(screen.getByRole('button', { name: '启用 上海锦棉' }));
    fireEvent.click(screen.getByRole('button', { name: '确认启用' }));
    fireEvent.click(screen.getByRole('button', { name: '删除 杭州云织' }));
    fireEvent.click(screen.getByRole('button', { name: '确认删除' }));

    await waitFor(() => expect(mockedDeactivateCustomer).toHaveBeenCalledWith('2051932034979037191'));
    expect(mockedActivateCustomer).toHaveBeenCalledWith('2051932034979037192');
    expect(mockedDeleteCustomer).toHaveBeenCalledWith('2051932034979037191');
  }, 10000);

  it('hides customer actions without permissions', async () => {
    mockedGetCurrentUserPermissions.mockResolvedValue({ menuTree: [], permissions: [] });
    setAuthSession({ userId: '1', username: 'viewer', realName: '只读用户', roleIds: [], permissions: [], menuTree: [] });
    mockedListCustomers.mockResolvedValue(customerPage);

    renderPage();

    await screen.findByText('杭州云织');
    expect(screen.queryByRole('button', { name: '新建客户' })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: '编辑 杭州云织' })).not.toBeInTheDocument();
  });
});

function renderPage() {
  render(
    <AntdApp>
      <CustomerManagementPage />
    </AntdApp>,
  );
}

function openDialogSelect(label: string) {
  const select = within(screen.getByRole('dialog')).getByLabelText(label);
  const input = select.querySelector('input') ?? select;
  fireEvent.focus(input);
  fireEvent.mouseDown(select.querySelector('.ant-select-selector') ?? select);
  fireEvent.keyDown(input, { key: 'ArrowDown', code: 'ArrowDown' });
}

async function chooseOption(label: string) {
  const optionLabel = (await screen.findAllByText(label)).at(-1)!;
  fireEvent.click(optionLabel.closest('.ant-select-item-option') ?? optionLabel);
}
