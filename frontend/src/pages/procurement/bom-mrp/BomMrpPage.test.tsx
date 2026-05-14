import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { App as AntdApp } from 'antd';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { BomMrpPage } from './BomMrpPage';
import { getCurrentUserPermissions } from '@/services/auth/authService';
import { approveBom, calculateMrp, getBomDetail, pageBoms, pageMrpResults } from '@/services/procurement/procurementService';
import { setAuthSession } from '@/shared/storage/authSessionStorage';

vi.mock('@/services/auth/authService', () => ({
  getCurrentUserPermissions: vi.fn(),
}));

vi.mock('@/services/procurement/procurementService', () => ({
  approveBom: vi.fn(),
  calculateMrp: vi.fn(),
  getBomDetail: vi.fn(),
  pageBoms: vi.fn(),
  pageMrpResults: vi.fn(),
}));

const mockedApproveBom = vi.mocked(approveBom);
const mockedCalculateMrp = vi.mocked(calculateMrp);
const mockedGetBomDetail = vi.mocked(getBomDetail);
const mockedGetCurrentUserPermissions = vi.mocked(getCurrentUserPermissions);
const mockedPageBoms = vi.mocked(pageBoms);
const mockedPageMrpResults = vi.mocked(pageMrpResults);

const permissions = ['procurement:bom:approve', 'procurement:mrp:calculate'];

const boms = [
  {
    id: '91001',
    code: 'BOM-SP-001',
    spuId: '80001',
    spuCode: 'SP20260001',
    spuName: '女士风衣',
    bomVersion: 1,
    status: 'DRAFT',
    statusLabel: '草稿',
    items: [],
  },
];

const bomDetail = {
  ...boms[0],
  items: [
    {
      id: '92001',
      materialId: '80001',
      materialCode: 'FAB-001',
      materialName: '高支棉',
      consumptionType: 'FIXED_PER_PIECE',
      consumptionTypeLabel: '固定用量',
      baseConsumption: 1.2,
      unit: '米',
      wastageRate: 0.05,
    },
  ],
};

const mrpResults = [
  {
    id: '30001',
    batchNo: 'MRP-20260510-0001',
    materialCode: 'FAB-001',
    materialName: '高支棉',
    grossDemand: 160,
    allocatedStock: 20,
    inTransitQuantity: 10,
    netDemand: 130,
    suggestedQuantity: 140,
    unit: '米',
    suggestedSupplierName: '经纬纺织',
    estimatedCost: 3500,
    status: 'PENDING',
    statusLabel: '待审核',
  },
];

describe('BomMrpPage', () => {
  beforeEach(() => {
    window.localStorage.clear();
    setAuthSession({ userId: '1', username: 'admin', realName: '系统管理员', roleIds: ['1'], permissions, menuTree: [] });
    mockedGetCurrentUserPermissions.mockReset();
    mockedGetCurrentUserPermissions.mockResolvedValue({ permissions, menuTree: [] });
    mockedPageBoms.mockReset();
    mockedPageBoms.mockResolvedValue({ current: 1, size: 10, total: 1, pages: 1, records: boms });
    mockedGetBomDetail.mockReset();
    mockedGetBomDetail.mockResolvedValue(bomDetail);
    mockedApproveBom.mockReset();
    mockedApproveBom.mockResolvedValue(undefined);
    mockedCalculateMrp.mockReset();
    mockedCalculateMrp.mockResolvedValue({ batchNo: 'MRP-20260510-0001', totalItems: 1 });
    mockedPageMrpResults.mockReset();
    mockedPageMrpResults.mockResolvedValue({ current: 1, size: 10, total: 1, pages: 1, records: mrpResults });
  });

  it('loads BOM list and opens BOM detail', async () => {
    renderPage();

    expect(screen.getByText('正在加载BOM')).toBeInTheDocument();
    expect(await screen.findByText('BOM-SP-001')).toBeInTheDocument();
    fireEvent.change(screen.getByPlaceholderText('款式ID'), { target: { value: ' 80001 ' } });
    fireEvent.click(screen.getByRole('button', { name: /搜索BOM/ }));

    await waitFor(() => expect(mockedPageBoms).toHaveBeenLastCalledWith(expect.objectContaining({ spuId: '80001' })));

    fireEvent.click(screen.getByRole('button', { name: '详情 BOM-SP-001' }));
    expect(await screen.findByText('高支棉')).toBeInTheDocument();
    expect(screen.getByText('固定用量')).toBeInTheDocument();
  });

  it('approves BOM and calculates MRP results', async () => {
    renderPage();

    await screen.findByText('BOM-SP-001');
    fireEvent.click(screen.getByRole('button', { name: '审批 BOM-SP-001' }));
    await waitFor(() => expect(mockedApproveBom).toHaveBeenCalledWith('91001'));

    fireEvent.click(screen.getByRole('tab', { name: 'MRP结果' }));
    expect(await screen.findByText('高支棉')).toBeInTheDocument();
    fireEvent.change(screen.getByPlaceholderText('生产订单ID，多个用逗号分隔'), { target: { value: ' 50001, 50002 ' } });
    fireEvent.click(screen.getByRole('button', { name: '执行MRP计算' }));

    await waitFor(() => expect(mockedCalculateMrp).toHaveBeenCalledWith({ productionOrderIds: ['50001', '50002'] }));
    await waitFor(() => expect(mockedPageMrpResults).toHaveBeenLastCalledWith(expect.objectContaining({ batchNo: 'MRP-20260510-0001' })));
  });
});

function renderPage() {
  render(
    <AntdApp>
      <BomMrpPage />
    </AntdApp>,
  );
}
