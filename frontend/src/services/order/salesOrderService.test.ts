import { beforeEach, describe, expect, it, vi } from 'vitest';
import {
  cancelSalesOrder,
  deleteSalesOrder,
  getSalesOrderDetail,
  pageSalesOrders,
  resubmitSalesOrder,
  submitSalesOrder,
} from './salesOrderService';
import { apiClient } from '@/services/http/apiClient';

vi.mock('@/services/http/apiClient', async () => {
  const actual = await vi.importActual<typeof import('@/services/http/apiClient')>('@/services/http/apiClient');
  return {
    ...actual,
    apiClient: {
      post: vi.fn(),
    },
  };
});

const mockedPost = vi.mocked(apiClient.post);

describe('salesOrderService', () => {
  beforeEach(() => {
    mockedPost.mockReset();
  });

  it('queries paged sales orders with normalized filters', async () => {
    mockedPost.mockResolvedValueOnce({
      data: { code: 0, message: 'success', success: true, data: { records: [], total: 0 } },
    });

    await pageSalesOrders({
      current: 0,
      size: 0,
      orderNo: ' SO ',
      status: '',
      orderDateStart: ' 2026-05-01 ',
      orderDateEnd: '',
    });

    expect(mockedPost).toHaveBeenCalledWith('/order/sales/page', {
      current: 1,
      size: 1,
      orderNo: 'SO',
      orderDateStart: '2026-05-01',
    });
  });

  it('loads detail and runs status actions', async () => {
    mockedPost
      .mockResolvedValueOnce({ data: { code: 0, message: 'success', success: true, data: { id: '10001' } } })
      .mockResolvedValueOnce({ data: { code: 0, message: 'success', success: true, data: null } })
      .mockResolvedValueOnce({ data: { code: 0, message: 'success', success: true, data: null } })
      .mockResolvedValueOnce({ data: { code: 0, message: 'success', success: true, data: null } })
      .mockResolvedValueOnce({ data: { code: 0, message: 'success', success: true, data: null } });

    await getSalesOrderDetail('10001');
    await submitSalesOrder('10001');
    await resubmitSalesOrder('10001');
    await cancelSalesOrder('10001');
    await deleteSalesOrder('10001');

    expect(mockedPost).toHaveBeenNthCalledWith(1, '/order/sales/detail', null, { params: { orderId: '10001' } });
    expect(mockedPost).toHaveBeenNthCalledWith(2, '/order/sales/submit', null, { params: { orderId: '10001' } });
    expect(mockedPost).toHaveBeenNthCalledWith(3, '/order/sales/resubmit', null, { params: { orderId: '10001' } });
    expect(mockedPost).toHaveBeenNthCalledWith(4, '/order/sales/cancel', null, { params: { orderId: '10001' } });
    expect(mockedPost).toHaveBeenNthCalledWith(5, '/order/sales/delete', null, { params: { orderId: '10001' } });
  });
});
