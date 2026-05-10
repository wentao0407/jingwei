import { beforeEach, describe, expect, it, vi } from 'vitest';
import { createReturnOrder, submitReturnOrder } from './returnOrderService';
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

describe('returnOrderService', () => {
  beforeEach(() => {
    mockedPost.mockReset();
  });

  it('creates return orders with normalized line matrices', async () => {
    mockedPost.mockResolvedValueOnce({
      data: { code: 0, message: 'success', success: true, data: { id: '71001', returnNo: 'RT-202605-0001' } },
    });

    await createReturnOrder({
      returnType: 'CUSTOMER_REJECT',
      salesOrderId: '10004',
      salesOrderNo: ' SO-202605-00004 ',
      customerId: '20001',
      reason: ' 尺码问题 ',
      remark: '',
      lines: [
        {
          salesOrderLineId: '90004',
          spuId: '80001',
          colorWayId: '70001',
          sizeMatrixJson: ' {"sizes":[{"sizeId":"50001","code":"S","quantity":3}]} ',
          totalQuantity: 3,
          remark: ' 合格退回 ',
        },
      ],
    });

    expect(mockedPost).toHaveBeenCalledWith('/order/return/create', {
      returnType: 'CUSTOMER_REJECT',
      salesOrderId: '10004',
      salesOrderNo: 'SO-202605-00004',
      customerId: '20001',
      reason: '尺码问题',
      lines: [
        {
          salesOrderLineId: '90004',
          spuId: '80001',
          colorWayId: '70001',
          sizeMatrixJson: '{"sizes":[{"sizeId":"50001","code":"S","quantity":3}]}',
          totalQuantity: 3,
          remark: '合格退回',
        },
      ],
    });
  });

  it('submits return orders by query parameter', async () => {
    mockedPost.mockResolvedValueOnce({ data: { code: 0, message: 'success', success: true, data: null } });

    await submitReturnOrder('71001');

    expect(mockedPost).toHaveBeenCalledWith('/order/return/submit', null, { params: { returnId: '71001' } });
  });
});
