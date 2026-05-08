import { fireEvent, render, screen, waitFor, within } from '@testing-library/react';
import { App as AntdApp } from 'antd';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { SupplierManagementPage } from './SupplierManagementPage';
import { getCurrentUserPermissions } from '@/services/auth/authService';
import { activateSupplier, createSupplier, deactivateSupplier, deleteSupplier, listSuppliers, updateSupplier } from '@/services/master/supplierService';
import { setAuthSession } from '@/shared/storage/authSessionStorage';

vi.mock('@/services/auth/authService', () => ({
  getCurrentUserPermissions: vi.fn(),
}));

vi.mock('@/services/master/supplierService', () => ({
  activateSupplier: vi.fn(),
  createSupplier: vi.fn(),
  deactivateSupplier: vi.fn(),
  deleteSupplier: vi.fn(),
  listSuppliers: vi.fn(),
  updateSupplier: vi.fn(),
}));

const mockedGetCurrentUserPermissions = vi.mocked(getCurrentUserPermissions);
const mockedActivateSupplier = vi.mocked(activateSupplier);
const mockedCreateSupplier = vi.mocked(createSupplier);
const mockedDeactivateSupplier = vi.mocked(deactivateSupplier);
const mockedDeleteSupplier = vi.mocked(deleteSupplier);
const mockedListSuppliers = vi.mocked(listSuppliers);
const mockedUpdateSupplier = vi.mocked(updateSupplier);
const supplierPermissions = [
  'master:supplier:create',
  'master:supplier:update',
  'master:supplier:activate',
  'master:supplier:deactivate',
  'master:supplier:delete',
];

const supplierPage = {
  records: [
    {
      id: '2051932034979037191',
      code: 'SUP-000001',
      name: '绍兴面料厂',
      shortName: '绍兴面料',
      type: 'FABRIC',
      contactPerson: '李经理',
      contactPhone: '13900139000',
      settlementType: 'MONTHLY',
      leadTimeDays: 7,
      qualificationStatus: 'PENDING',
      status: 'ACTIVE',
    },
    {
      id: '2051932034979037192',
      code: 'SUP-000002',
      name: '宁波辅料厂',
      type: 'TRIM',
      qualificationStatus: 'QUALIFIED',
      status: 'INACTIVE',
    },
  ],
  total: 2,
  current: 1,
  size: 10,
  pages: 1,
};

describe('SupplierManagementPage', () => {
  beforeEach(() => {
    window.localStorage.clear();
    mockedGetCurrentUserPermissions.mockReset();
    mockedGetCurrentUserPermissions.mockResolvedValue({ menuTree: [], permissions: supplierPermissions });
    mockedActivateSupplier.mockReset();
    mockedCreateSupplier.mockReset();
    mockedDeactivateSupplier.mockReset();
    mockedDeleteSupplier.mockReset();
    mockedListSuppliers.mockReset();
    mockedUpdateSupplier.mockReset();
  });

  it('loads and renders paged suppliers', async () => {
    mockedListSuppliers.mockResolvedValue(supplierPage);

    renderPage();

    expect(screen.getByText('正在加载供应商数据')).toBeInTheDocument();
    expect(await screen.findByText('绍兴面料厂')).toBeInTheDocument();
    expect(screen.getByText('SUP-000001')).toBeInTheDocument();
    expect(screen.getByText('面料供应商')).toBeInTheDocument();
    expect(screen.getByText('待审核')).toBeInTheDocument();
  });

  it('searches and filters suppliers', async () => {
    mockedListSuppliers.mockResolvedValue(supplierPage);

    renderPage();

    await screen.findByText('绍兴面料厂');
    fireEvent.change(screen.getByPlaceholderText('搜索供应商编码/名称'), { target: { value: ' 面料 ' } });
    fireEvent.click(screen.getByRole('button', { name: /查询/ }));

    await waitFor(() =>
      expect(mockedListSuppliers).toHaveBeenLastCalledWith({
        current: 1,
        size: 10,
        keyword: '面料',
      }),
    );
  });

  it('creates and updates suppliers', async () => {
    mockedListSuppliers.mockResolvedValue(supplierPage);
    mockedCreateSupplier.mockResolvedValue(supplierPage.records[0]);
    mockedUpdateSupplier.mockResolvedValue(supplierPage.records[0]);

    renderPage();

    await screen.findByText('绍兴面料厂');
    fireEvent.click(screen.getByRole('button', { name: '新建供应商' }));
    fireEvent.change(screen.getByLabelText('供应商名称'), { target: { value: ' 苏州包装厂 ' } });
    openDialogSelect('供应商类型');
    await chooseOption('包装供应商');
    fireEvent.change(screen.getByLabelText('联系电话'), { target: { value: '13900139000' } });
    fireEvent.change(screen.getByLabelText('平均交货天数'), { target: { value: '5' } });
    fireEvent.click(screen.getByRole('button', { name: '保存' }));

    await waitFor(() =>
      expect(mockedCreateSupplier).toHaveBeenCalledWith({
        name: '苏州包装厂',
        type: 'PACKAGING',
        contactPhone: '13900139000',
        leadTimeDays: 5,
      }),
    );

    fireEvent.click(screen.getByRole('button', { name: '编辑 绍兴面料厂' }));
    fireEvent.change(screen.getByLabelText('供应商名称'), { target: { value: ' 绍兴面料厂更新 ' } });
    openDialogSelect('资质状态');
    await chooseOption('合格');
    fireEvent.click(screen.getByRole('button', { name: '保存' }));

    await waitFor(() =>
      expect(mockedUpdateSupplier).toHaveBeenCalledWith('2051932034979037191', expect.objectContaining({ name: '绍兴面料厂更新', qualificationStatus: 'QUALIFIED' })),
    );
  });

  it('validates phone before saving suppliers', async () => {
    mockedListSuppliers.mockResolvedValue(supplierPage);

    renderPage();

    await screen.findByText('绍兴面料厂');
    fireEvent.click(screen.getByRole('button', { name: '新建供应商' }));
    fireEvent.change(screen.getByLabelText('供应商名称'), { target: { value: '测试供应商' } });
    openDialogSelect('供应商类型');
    await chooseOption('面料供应商');
    fireEvent.change(screen.getByLabelText('联系电话'), { target: { value: '123' } });
    fireEvent.click(screen.getByRole('button', { name: '保存' }));

    expect(await screen.findByText('请输入正确的手机号')).toBeInTheDocument();
    expect(mockedCreateSupplier).not.toHaveBeenCalled();
  });

  it('activates, deactivates and deletes suppliers', async () => {
    mockedListSuppliers.mockResolvedValue(supplierPage);
    mockedActivateSupplier.mockResolvedValue(undefined);
    mockedDeactivateSupplier.mockResolvedValue(undefined);
    mockedDeleteSupplier.mockResolvedValue(undefined);

    renderPage();

    await screen.findByText('绍兴面料厂');
    fireEvent.click(screen.getByRole('button', { name: '停用 绍兴面料厂' }));
    fireEvent.click(screen.getByRole('button', { name: '确认停用' }));
    fireEvent.click(screen.getByRole('button', { name: '启用 宁波辅料厂' }));
    fireEvent.click(screen.getByRole('button', { name: '确认启用' }));
    fireEvent.click(screen.getByRole('button', { name: '删除 绍兴面料厂' }));
    fireEvent.click(screen.getByRole('button', { name: '确认删除' }));

    await waitFor(() => expect(mockedDeactivateSupplier).toHaveBeenCalledWith('2051932034979037191'));
    expect(mockedActivateSupplier).toHaveBeenCalledWith('2051932034979037192');
    expect(mockedDeleteSupplier).toHaveBeenCalledWith('2051932034979037191');
  });

  it('hides supplier actions without permissions', async () => {
    mockedGetCurrentUserPermissions.mockResolvedValue({ menuTree: [], permissions: [] });
    setAuthSession({ userId: '1', username: 'viewer', realName: '只读用户', roleIds: [], permissions: [], menuTree: [] });
    mockedListSuppliers.mockResolvedValue(supplierPage);

    renderPage();

    await screen.findByText('绍兴面料厂');
    expect(screen.queryByRole('button', { name: '新建供应商' })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: '编辑 绍兴面料厂' })).not.toBeInTheDocument();
  });
});

function renderPage() {
  render(
    <AntdApp>
      <SupplierManagementPage />
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
