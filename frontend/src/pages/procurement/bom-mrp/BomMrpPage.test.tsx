import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { App as AntdApp } from 'antd';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { BomMrpPage } from './BomMrpPage';
import { getCurrentUserPermissions } from '@/services/auth/authService';
import {
  approveBom,
  calculateMrp,
  createBom,
  deleteBom,
  getBomDetail,
  pageBoms,
  pageMrpResults,
  updateBom,
} from '@/services/procurement/procurementService';
import { setAuthSession } from '@/shared/storage/authSessionStorage';

vi.mock('@/services/auth/authService', () => ({
  getCurrentUserPermissions: vi.fn(),
}));

vi.mock('@/services/procurement/procurementService', () => ({
  approveBom: vi.fn(),
  calculateMrp: vi.fn(),
  createBom: vi.fn(),
  deleteBom: vi.fn(),
  getBomDetail: vi.fn(),
  pageBoms: vi.fn(),
  pageMrpResults: vi.fn(),
  updateBom: vi.fn(),
}));

const mockedApproveBom = vi.mocked(approveBom);
const mockedCalculateMrp = vi.mocked(calculateMrp);
const mockedCreateBom = vi.mocked(createBom);
const mockedDeleteBom = vi.mocked(deleteBom);
const mockedGetBomDetail = vi.mocked(getBomDetail);
const mockedGetCurrentUserPermissions = vi.mocked(getCurrentUserPermissions);
const mockedPageBoms = vi.mocked(pageBoms);
const mockedPageMrpResults = vi.mocked(pageMrpResults);
const mockedUpdateBom = vi.mocked(updateBom);

const permissions = [
  'procurement:bom:approve',
  'procurement:bom:create',
  'procurement:bom:delete',
  'procurement:bom:update',
  'procurement:mrp:calculate',
];

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
      materialType: 'FABRIC',
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
    mockedCreateBom.mockReset();
    mockedCreateBom.mockResolvedValue({ ...boms[0], id: '91002', code: 'BOM-SP-002' });
    mockedDeleteBom.mockReset();
    mockedDeleteBom.mockResolvedValue(undefined);
    mockedPageMrpResults.mockReset();
    mockedPageMrpResults.mockResolvedValue({ current: 1, size: 10, total: 1, pages: 1, records: mrpResults });
    mockedUpdateBom.mockReset();
    mockedUpdateBom.mockResolvedValue(bomDetail);
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

  it('creates updates and deletes BOM records from the BOM panel', async () => {
    renderPage();

    await screen.findByText('BOM-SP-001');
    fireEvent.click(screen.getByRole('button', { name: '新增BOM' }));
    fireEvent.change(screen.getByLabelText('款式ID'), { target: { value: ' 80002 ' } });
    fireEvent.change(screen.getByLabelText('生效日期'), { target: { value: ' 2026-05-18 ' } });
    fireEvent.change(screen.getByLabelText('物料ID'), { target: { value: '30001' } });
    fireEvent.change(screen.getByLabelText('物料类型'), { target: { value: ' FABRIC ' } });
    fireEvent.change(screen.getByLabelText('消耗类型'), { target: { value: ' FIXED_PER_PIECE ' } });
    fireEvent.change(screen.getByLabelText('基准用量'), { target: { value: '1.2' } });
    fireEvent.change(screen.getByLabelText('单位'), { target: { value: ' 米 ' } });
    fireEvent.change(screen.getByLabelText('损耗率'), { target: { value: '0.08' } });
    fireEvent.click(screen.getByRole('button', { name: '保存BOM' }));

    await waitFor(() => expect(mockedCreateBom).toHaveBeenCalledWith({
      spuId: '80002',
      effectiveFrom: '2026-05-18',
      items: [
        {
          materialId: '30001',
          materialType: 'FABRIC',
          consumptionType: 'FIXED_PER_PIECE',
          baseConsumption: 1.2,
          unit: '米',
          wastageRate: 0.08,
        },
      ],
    }));

    fireEvent.click(screen.getByRole('button', { name: '编辑 BOM-SP-001' }));
    fireEvent.change(await screen.findByLabelText('BOM备注'), { target: { value: ' 更新BOM ' } });
    fireEvent.click(screen.getByRole('button', { name: '保存BOM' }));
    await waitFor(() => expect(mockedUpdateBom).toHaveBeenCalledWith('91001', expect.objectContaining({
      remark: '更新BOM',
    })));

    fireEvent.click(screen.getByRole('button', { name: '删除 BOM-SP-001' }));
    fireEvent.click(await screen.findByRole('button', { name: '确认删除' }));
    await waitFor(() => expect(mockedDeleteBom).toHaveBeenCalledWith('91001'));
  });
});

function renderPage() {
  render(
    <AntdApp>
      <BomMrpPage />
    </AntdApp>,
  );
}
