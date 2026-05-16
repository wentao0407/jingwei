import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { App as AntdApp } from 'antd';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { ReturnOrderListPage } from './ReturnOrderListPage';
import {
  approveReturnOrder,
  confirmReturnReceive,
  getReturnOrderDetail,
  pageReturnOrders,
  processReturnQc,
  rejectReturnOrder,
  submitReturnOrder,
} from '@/services/order/returnOrderService';
import { setAuthSession } from '@/shared/storage/authSessionStorage';

vi.mock('@/services/order/returnOrderService', () => ({
  approveReturnOrder: vi.fn(),
  confirmReturnReceive: vi.fn(),
  getReturnOrderDetail: vi.fn(),
  pageReturnOrders: vi.fn(),
  processReturnQc: vi.fn(),
  rejectReturnOrder: vi.fn(),
  submitReturnOrder: vi.fn(),
}));

const mockedApproveReturnOrder = vi.mocked(approveReturnOrder);
const mockedConfirmReturnReceive = vi.mocked(confirmReturnReceive);
const mockedGetReturnOrderDetail = vi.mocked(getReturnOrderDetail);
const mockedPageReturnOrders = vi.mocked(pageReturnOrders);
const mockedProcessReturnQc = vi.mocked(processReturnQc);
const mockedRejectReturnOrder = vi.mocked(rejectReturnOrder);
const mockedSubmitReturnOrder = vi.mocked(submitReturnOrder);

const returnOrder = { id: '71001', returnNo: 'RT-202605-001', salesOrderNo: 'SO-001', status: 'DRAFT', statusLabel: '草稿' };
const returnDetail = {
  ...returnOrder,
  lines: [{ id: '72001', totalQuantity: 10, qcPassedQty: 0, qcFailedQty: 0, spuId: '80001', colorWayId: '81001' }],
};

describe('ReturnOrderListPage', () => {
  beforeEach(() => {
    window.localStorage.clear();
    setAuthSession({
      userId: '1',
      username: 'admin',
      realName: '系统管理员',
      roleIds: ['1'],
      permissions: ['order:return:submit', 'order:return:approve', 'order:return:receive', 'order:return:qc'],
      menuTree: [],
    });
    mockedPageReturnOrders.mockReset();
    mockedPageReturnOrders.mockResolvedValue({
      records: [returnOrder],
      total: 1,
      current: 1,
      size: 20,
      pages: 1,
    });
    mockedGetReturnOrderDetail.mockReset();
    mockedGetReturnOrderDetail.mockResolvedValue(returnDetail);
    mockedSubmitReturnOrder.mockReset();
    mockedSubmitReturnOrder.mockResolvedValue(undefined);
    mockedApproveReturnOrder.mockReset();
    mockedApproveReturnOrder.mockResolvedValue(undefined);
    mockedRejectReturnOrder.mockReset();
    mockedRejectReturnOrder.mockResolvedValue(undefined);
    mockedConfirmReturnReceive.mockReset();
    mockedConfirmReturnReceive.mockResolvedValue(undefined);
    mockedProcessReturnQc.mockReset();
    mockedProcessReturnQc.mockResolvedValue(undefined);
  });

  it('loads return orders', async () => {
    renderPage();

    expect(await screen.findByText('RT-202605-001')).toBeInTheDocument();
    expect(screen.getByText('SO-001')).toBeInTheDocument();
    expect(mockedPageReturnOrders).toHaveBeenCalledWith({ current: 1, size: 20 });
  });

  it('opens detail and performs return lifecycle actions', async () => {
    renderPage();

    await screen.findByText('RT-202605-001');
    fireEvent.click(screen.getByRole('button', { name: '详情 RT-202605-001' }));
    expect(await screen.findByText('72001')).toBeInTheDocument();

    fireEvent.click(screen.getByRole('button', { name: '提交审批' }));
    await waitFor(() => expect(mockedSubmitReturnOrder).toHaveBeenCalledWith('71001'));

    fireEvent.click(screen.getByRole('button', { name: '审批通过' }));
    await waitFor(() => expect(mockedApproveReturnOrder).toHaveBeenCalledWith('71001'));

    fireEvent.click(screen.getByRole('button', { name: '确认收货' }));
    await waitFor(() => expect(mockedConfirmReturnReceive).toHaveBeenCalledWith('71001'));

    fireEvent.click(screen.getByRole('button', { name: '退货质检' }));
    fireEvent.change(screen.getByLabelText('合格数量'), { target: { value: '8' } });
    fireEvent.change(screen.getByLabelText('不合格数量'), { target: { value: '2' } });
    fireEvent.change(screen.getByLabelText('质检结论'), { target: { value: ' 可返库 ' } });
    fireEvent.click(screen.getByRole('button', { name: '提交质检' }));
    await waitFor(() => expect(mockedProcessReturnQc).toHaveBeenCalledWith({
      returnId: '71001',
      results: [{ lineId: '72001', passedQty: 8, failedQty: 2, qcResult: '可返库' }],
    }));
  });
});

function renderPage() {
  render(
    <AntdApp>
      <ReturnOrderListPage />
    </AntdApp>,
  );
}
