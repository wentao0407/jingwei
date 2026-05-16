import { render, screen } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { ReturnOrderListPage } from './ReturnOrderListPage';
import { pageReturnOrders } from '@/services/order/returnOrderService';

vi.mock('@/services/order/returnOrderService', () => ({
  pageReturnOrders: vi.fn(),
}));

const mockedPageReturnOrders = vi.mocked(pageReturnOrders);

describe('ReturnOrderListPage', () => {
  beforeEach(() => {
    mockedPageReturnOrders.mockReset();
    mockedPageReturnOrders.mockResolvedValue({
      records: [{ id: '71001', returnNo: 'RT-202605-001', salesOrderNo: 'SO-001', statusLabel: '待审批' }],
      total: 1,
      current: 1,
      size: 20,
      pages: 1,
    });
  });

  it('loads return orders', async () => {
    render(<ReturnOrderListPage />);

    expect(await screen.findByText('RT-202605-001')).toBeInTheDocument();
    expect(screen.getByText('SO-001')).toBeInTheDocument();
    expect(mockedPageReturnOrders).toHaveBeenCalledWith({ current: 1, size: 20 });
  });
});
