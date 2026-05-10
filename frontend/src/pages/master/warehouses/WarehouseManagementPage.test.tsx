import { fireEvent, render, screen, waitFor, within } from '@testing-library/react';
import { App as AntdApp } from 'antd';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { WarehouseManagementPage } from './WarehouseManagementPage';
import { getCurrentUserPermissions } from '@/services/auth/authService';
import {
  activateWarehouse,
  createLocation,
  createWarehouse,
  deactivateLocation,
  deactivateWarehouse,
  deleteLocation,
  deleteWarehouse,
  freezeLocation,
  getWarehouseDetail,
  pageWarehouses,
  unfreezeLocation,
  updateLocation,
  updateWarehouse,
} from '@/services/master/warehouseService';
import { setAuthSession } from '@/shared/storage/authSessionStorage';

vi.mock('@/services/auth/authService', () => ({
  getCurrentUserPermissions: vi.fn(),
}));

vi.mock('@/services/master/warehouseService', () => ({
  activateWarehouse: vi.fn(),
  createLocation: vi.fn(),
  createWarehouse: vi.fn(),
  deactivateLocation: vi.fn(),
  deactivateWarehouse: vi.fn(),
  deleteLocation: vi.fn(),
  deleteWarehouse: vi.fn(),
  freezeLocation: vi.fn(),
  getWarehouseDetail: vi.fn(),
  pageWarehouses: vi.fn(),
  unfreezeLocation: vi.fn(),
  updateLocation: vi.fn(),
  updateWarehouse: vi.fn(),
}));

const mockedGetCurrentUserPermissions = vi.mocked(getCurrentUserPermissions);
const mockedActivateWarehouse = vi.mocked(activateWarehouse);
const mockedCreateLocation = vi.mocked(createLocation);
const mockedCreateWarehouse = vi.mocked(createWarehouse);
const mockedDeactivateLocation = vi.mocked(deactivateLocation);
const mockedDeactivateWarehouse = vi.mocked(deactivateWarehouse);
const mockedDeleteLocation = vi.mocked(deleteLocation);
const mockedDeleteWarehouse = vi.mocked(deleteWarehouse);
const mockedFreezeLocation = vi.mocked(freezeLocation);
const mockedGetWarehouseDetail = vi.mocked(getWarehouseDetail);
const mockedPageWarehouses = vi.mocked(pageWarehouses);
const mockedUnfreezeLocation = vi.mocked(unfreezeLocation);
const mockedUpdateLocation = vi.mocked(updateLocation);
const mockedUpdateWarehouse = vi.mocked(updateWarehouse);

const permissions = [
  'master:warehouse:create',
  'master:warehouse:update',
  'master:warehouse:activate',
  'master:warehouse:deactivate',
  'master:warehouse:delete',
  'master:location:create',
  'master:location:update',
  'master:location:freeze',
  'master:location:unfreeze',
  'master:location:deactivate',
  'master:location:delete',
];

const warehouses = [
  { id: '10001', code: 'WH01', name: '成品主仓', type: 'FINISHED_GOODS', status: 'ACTIVE', address: '上海' },
  { id: '10002', code: 'WH02', name: '原料仓', type: 'RAW_MATERIAL', status: 'INACTIVE', address: '苏州' },
];

const warehouseDetail = {
  ...warehouses[0],
  locations: [
    {
      id: '20001',
      warehouseId: '10001',
      zoneCode: 'A',
      rackCode: '01',
      rowCode: '02',
      binCode: '03',
      fullCode: 'WH01-A-01-02-03',
      locationType: 'STORAGE',
      capacity: 100,
      usedCapacity: 20,
      status: 'ACTIVE',
    },
    {
      id: '20002',
      warehouseId: '10001',
      zoneCode: 'B',
      rackCode: '01',
      rowCode: '01',
      binCode: '01',
      fullCode: 'WH01-B-01-01-01',
      locationType: 'QC',
      capacity: 50,
      usedCapacity: 0,
      status: 'FROZEN',
    },
  ],
};

describe('WarehouseManagementPage', () => {
  beforeEach(() => {
    window.localStorage.clear();
    mockedGetCurrentUserPermissions.mockReset();
    mockedGetCurrentUserPermissions.mockResolvedValue({ menuTree: [], permissions });
    mockedActivateWarehouse.mockReset();
    mockedActivateWarehouse.mockResolvedValue(undefined);
    mockedCreateLocation.mockReset();
    mockedCreateLocation.mockResolvedValue(warehouseDetail.locations[0]);
    mockedCreateWarehouse.mockReset();
    mockedCreateWarehouse.mockResolvedValue(warehouses[0]);
    mockedDeactivateLocation.mockReset();
    mockedDeactivateLocation.mockResolvedValue(undefined);
    mockedDeactivateWarehouse.mockReset();
    mockedDeactivateWarehouse.mockResolvedValue(undefined);
    mockedDeleteLocation.mockReset();
    mockedDeleteLocation.mockResolvedValue(undefined);
    mockedDeleteWarehouse.mockReset();
    mockedDeleteWarehouse.mockResolvedValue(undefined);
    mockedFreezeLocation.mockReset();
    mockedFreezeLocation.mockResolvedValue(undefined);
    mockedGetWarehouseDetail.mockReset();
    mockedGetWarehouseDetail.mockResolvedValue(warehouseDetail);
    mockedPageWarehouses.mockReset();
    mockedPageWarehouses.mockResolvedValue({ current: 1, size: 10, total: 2, pages: 1, records: warehouses });
    mockedUnfreezeLocation.mockReset();
    mockedUnfreezeLocation.mockResolvedValue(undefined);
    mockedUpdateLocation.mockReset();
    mockedUpdateLocation.mockResolvedValue(warehouseDetail.locations[0]);
    mockedUpdateWarehouse.mockReset();
    mockedUpdateWarehouse.mockResolvedValue(warehouses[0]);
  });

  it('loads and filters warehouses', async () => {
    renderPage();

    expect(screen.getByText('正在加载仓库数据')).toBeInTheDocument();
    expect(await screen.findByText('成品主仓')).toBeInTheDocument();
    fireEvent.change(screen.getByPlaceholderText('搜索仓库编码或名称'), { target: { value: ' WH01 ' } });
    fireEvent.click(screen.getByRole('button', { name: '搜索' }));
    openSelect('仓库类型筛选');
    await chooseOption('成品仓');

    await waitFor(() =>
      expect(mockedPageWarehouses).toHaveBeenLastCalledWith(expect.objectContaining({ keyword: 'WH01', type: 'FINISHED_GOODS' })),
    );
  });

  it('creates, updates, activates, deactivates and deletes warehouses', async () => {
    renderPage();

    await screen.findByText('成品主仓');
    fireEvent.click(screen.getByRole('button', { name: '新建仓库' }));
    fireEvent.change(screen.getByLabelText('仓库编码'), { target: { value: ' WH03 ' } });
    fireEvent.change(screen.getByLabelText('仓库名称'), { target: { value: ' 退货仓 ' } });
    openDialogSelect('仓库类型');
    await chooseOption('退货仓');
    fireEvent.click(screen.getByRole('button', { name: '保存' }));

    await waitFor(() => expect(mockedCreateWarehouse).toHaveBeenCalledWith(expect.objectContaining({ code: 'WH03', name: '退货仓' })));

    fireEvent.click(screen.getByRole('button', { name: '编辑 成品主仓' }));
    fireEvent.change(screen.getByLabelText('仓库名称'), { target: { value: ' 成品主仓更新 ' } });
    fireEvent.click(screen.getByRole('button', { name: '保存' }));
    await waitFor(() => expect(mockedUpdateWarehouse).toHaveBeenCalledWith('10001', expect.objectContaining({ name: '成品主仓更新' })));

    fireEvent.click(screen.getByRole('button', { name: '停用 成品主仓' }));
    fireEvent.click(screen.getByRole('button', { name: '确认停用' }));
    await waitFor(() => expect(mockedDeactivateWarehouse).toHaveBeenCalledWith('10001'));

    fireEvent.click(screen.getByRole('button', { name: '启用 原料仓' }));
    await waitFor(() => expect(mockedActivateWarehouse).toHaveBeenCalledWith('10002'));

    fireEvent.click(screen.getByRole('button', { name: '删除 原料仓' }));
    fireEvent.click(screen.getByRole('button', { name: '确认删除' }));
    await waitFor(() => expect(mockedDeleteWarehouse).toHaveBeenCalledWith('10002'));
  });

  it('manages locations inside selected warehouse', async () => {
    renderPage();

    await screen.findByText('成品主仓');
    fireEvent.click(screen.getByRole('button', { name: '库位 成品主仓' }));
    expect(await screen.findByText('WH01-A-01-02-03')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: '新增库位' }));
    fireEvent.change(screen.getByLabelText('库区编码'), { target: { value: ' C ' } });
    fireEvent.change(screen.getByLabelText('货架编码'), { target: { value: ' 02 ' } });
    fireEvent.change(screen.getByLabelText('层编码'), { target: { value: ' 01 ' } });
    fireEvent.change(screen.getByLabelText('位编码'), { target: { value: ' 09 ' } });
    openDialogSelect('库位类型');
    await chooseOption('拣货位');
    fireEvent.change(screen.getByLabelText('容量'), { target: { value: '80' } });
    fireEvent.click(screen.getByRole('button', { name: '保存' }));

    await waitFor(() => expect(mockedCreateLocation).toHaveBeenCalledWith('10001', expect.objectContaining({ zoneCode: 'C' })));

    fireEvent.click(screen.getByRole('button', { name: '编辑库位 WH01-A-01-02-03' }));
    fireEvent.change(screen.getByLabelText('容量'), { target: { value: '120' } });
    fireEvent.click(screen.getByRole('button', { name: '保存' }));
    await waitFor(() => expect(mockedUpdateLocation).toHaveBeenCalledWith('20001', expect.objectContaining({ capacity: 120 })));

    fireEvent.click(screen.getByRole('button', { name: '冻结库位 WH01-A-01-02-03' }));
    await waitFor(() => expect(mockedFreezeLocation).toHaveBeenCalledWith('20001'));

    fireEvent.click(screen.getByRole('button', { name: '解冻库位 WH01-B-01-01-01' }));
    await waitFor(() => expect(mockedUnfreezeLocation).toHaveBeenCalledWith('20002'));

    fireEvent.click(screen.getByRole('button', { name: '停用库位 WH01-A-01-02-03' }));
    await waitFor(() => expect(mockedDeactivateLocation).toHaveBeenCalledWith('20001'));

    fireEvent.click(screen.getByRole('button', { name: '删除库位 WH01-A-01-02-03' }));
    fireEvent.click(screen.getByRole('button', { name: '确认删除' }));
    await waitFor(() => expect(mockedDeleteLocation).toHaveBeenCalledWith('20001'));
  }, 10000);

  it('hides warehouse actions without permissions', async () => {
    mockedGetCurrentUserPermissions.mockResolvedValue({ menuTree: [], permissions: [] });
    setAuthSession({ userId: '1', username: 'viewer', realName: '只读用户', roleIds: [], permissions: [], menuTree: [] });

    renderPage();

    await screen.findByText('成品主仓');
    expect(screen.queryByRole('button', { name: '新建仓库' })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: '编辑 成品主仓' })).not.toBeInTheDocument();
  });
});

function renderPage() {
  render(
    <AntdApp>
      <WarehouseManagementPage />
    </AntdApp>,
  );
}

function openSelect(label: string) {
  const select = screen.getAllByLabelText(label).find((element) => element.classList.contains('ant-select')) ?? screen.getByLabelText(label);
  fireEvent.mouseDown(select.querySelector('.ant-select-selector') ?? select);
}

function openDialogSelect(label: string) {
  const dialog = screen.getAllByRole('dialog').at(-1)!;
  const matches = within(dialog).getAllByLabelText(label);
  const select = matches.find((element) => element.classList.contains('ant-select')) ?? matches.at(-1)!;
  fireEvent.mouseDown(select.querySelector('.ant-select-selector') ?? select);
}

async function chooseOption(label: string) {
  const optionLabel = (await screen.findAllByText(label)).at(-1)!;
  fireEvent.click(optionLabel.closest('.ant-select-item-option') ?? optionLabel);
}
