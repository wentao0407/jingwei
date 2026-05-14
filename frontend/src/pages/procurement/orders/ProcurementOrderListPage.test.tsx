import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { App as AntdApp } from 'antd';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { ProcurementOrderListPage } from './ProcurementOrderListPage';
import { getCurrentUserPermissions } from '@/services/auth/authService';
import {
  fireProcurementOrderEvent,
  getProcurementOrderAvailableActions,
  getProcurementOrderDetail,
  pageProcurementOrders,
} from '@/services/procurement/procurementService';
import { setAuthSession } from '@/shared/storage/authSessionStorage';

vi.mock('@/services/auth/authService', () => ({
  getCurrentUserPermissions: vi.fn(),
}));

vi.mock('@/services/procurement/procurementService', () => ({
  fireProcurementOrderEvent: vi.fn(),
  getProcurementOrderAvailableActions: vi.fn(),
  getProcurementOrderDetail: vi.fn(),
  pageProcurementOrders: vi.fn(),
}));

const mockedFireProcurementOrderEvent = vi.mocked(fireProcurementOrderEvent);
const mockedGetCurrentUserPermissions = vi.mocked(getCurrentUserPermissions);
const mockedGetProcurementOrderAvailableActions = vi.mocked(getProcurementOrderAvailableActions);
const mockedGetProcurementOrderDetail = vi.mocked(getProcurementOrderDetail);
const mockedPageProcurementOrders = vi.mocked(pageProcurementOrders);

const permissions = ['procurement:order:fire-event'];

const procurementOrders = [
  {
    id: '70001',
    orderNo: 'PO-202605-00001',
    supplierId: '90001',
    supplierName: '经纬纺织',
    orderDate: '2026-05-10',
    expectedDeliveryDate: '2026-05-20',
    status: 'DRAFT',
    statusLabel: '草稿',
    totalAmount: 3600,
    paymentStatus: 'UNPAID',
    mrpBatchNo: 'MRP-20260510-0001',
    lines: [],
  },
];

const procurementOrderDetail = {
  ...procurementOrders[0],
  lines: [
    {
      id: '71001',
      lineNo: 1,
      materialId: '80001',
      materialCode: 'FAB-001',
      materialName: '高支棉',
      materialType: 'MATERIAL',
      quantity: 120,
      unit: '米',
      unitPrice: 30,
      lineAmount: 3600,
      deliveredQuantity: 20,
      acceptedQuantity: 18,
      rejectedQuantity: 2,
      mrpResultId: '30001',
    },
  ],
};

describe('ProcurementOrderListPage', () => {
  beforeEach(() => {
    window.localStorage.clear();
    setAuthSession({ userId: '1', username: 'admin', realName: '系统管理员', roleIds: ['1'], permissions, menuTree: [] });
    mockedGetCurrentUserPermissions.mockReset();
    mockedGetCurrentUserPermissions.mockResolvedValue({ permissions, menuTree: [] });
    mockedPageProcurementOrders.mockReset();
    mockedPageProcurementOrders.mockResolvedValue({ current: 1, size: 10, total: 1, pages: 1, records: procurementOrders });
    mockedGetProcurementOrderDetail.mockReset();
    mockedGetProcurementOrderDetail.mockResolvedValue(procurementOrderDetail);
    mockedGetProcurementOrderAvailableActions.mockReset();
    mockedGetProcurementOrderAvailableActions.mockResolvedValue(['SUBMIT']);
    mockedFireProcurementOrderEvent.mockReset();
    mockedFireProcurementOrderEvent.mockResolvedValue(undefined);
  });

  it('loads filters and opens procurement order detail', async () => {
    renderPage();

    expect(screen.getByText('正在加载采购订单')).toBeInTheDocument();
    expect(await screen.findByText('PO-202605-00001')).toBeInTheDocument();
    fireEvent.change(screen.getByPlaceholderText('供应商ID'), { target: { value: ' 90001 ' } });
    fireEvent.click(screen.getByRole('button', { name: /搜索/ }));

    await waitFor(() => expect(mockedPageProcurementOrders).toHaveBeenLastCalledWith(expect.objectContaining({ supplierId: '90001' })));

    fireEvent.click(screen.getByRole('button', { name: '详情 PO-202605-00001' }));
    expect(await screen.findByText('高支棉')).toBeInTheDocument();
    expect(screen.getByText('MRP-20260510-0001')).toBeInTheDocument();
  });

  it('fires available procurement order action with permission', async () => {
    renderPage();

    await screen.findByText('PO-202605-00001');
    fireEvent.click(screen.getByRole('button', { name: '详情 PO-202605-00001' }));
    fireEvent.click(await screen.findByRole('button', { name: '提交审批' }));

    await waitFor(() => expect(mockedFireProcurementOrderEvent).toHaveBeenCalledWith({ orderId: '70001', event: 'SUBMIT' }));
  });

  it('hides order event actions without permission', async () => {
    mockedGetCurrentUserPermissions.mockResolvedValue({ permissions: [], menuTree: [] });
    setAuthSession({ userId: '1', username: 'viewer', realName: '只读用户', roleIds: [], permissions: [], menuTree: [] });

    renderPage();

    await screen.findByText('PO-202605-00001');
    fireEvent.click(screen.getByRole('button', { name: '详情 PO-202605-00001' }));
    expect(await screen.findByText('高支棉')).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: '提交审批' })).not.toBeInTheDocument();
  });
});

function renderPage() {
  render(
    <AntdApp>
      <ProcurementOrderListPage />
    </AntdApp>,
  );
}
