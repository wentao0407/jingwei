import { beforeEach, describe, expect, it, vi } from 'vitest';
import {
  approveReturnOrder,
  confirmReturnReceive,
  createReturnOrder,
  getReturnOrderDetail,
  pageReturnOrders,
  processReturnQc,
  rejectReturnOrder,
  submitReturnOrder,
} from './returnOrderService';
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

  it('loads return order detail by query parameter', async () => {
    mockedPost.mockResolvedValueOnce({
      data: { code: 0, message: 'success', success: true, data: { id: '71001', returnNo: 'RT-202605-0001' } },
    });

    await getReturnOrderDetail(' 71001 ');

    expect(mockedPost).toHaveBeenCalledWith('/order/return/detail', null, { params: { returnId: '71001' } });
  });

  it('fires return order status operations by query parameter', async () => {
    mockedPost.mockResolvedValue({ data: { code: 0, message: 'success', success: true, data: null } });

    await approveReturnOrder(' 71001 ');
    await rejectReturnOrder('71002');
    await confirmReturnReceive('71003');

    expect(mockedPost).toHaveBeenNthCalledWith(1, '/order/return/approve', null, { params: { returnId: '71001' } });
    expect(mockedPost).toHaveBeenNthCalledWith(2, '/order/return/reject', null, { params: { returnId: '71002' } });
    expect(mockedPost).toHaveBeenNthCalledWith(3, '/order/return/receive', null, { params: { returnId: '71003' } });
  });

  it('submits return quality control with normalized result lines', async () => {
    mockedPost.mockResolvedValueOnce({ data: { code: 0, message: 'success', success: true, data: null } });

    await processReturnQc({
      returnId: ' 71001 ',
      results: [
        {
          lineId: ' 72001 ',
          passedQty: 2,
          failedQty: 1,
          qcResult: ' PASS_WITH_REWORK ',
          remark: '',
        },
      ],
    });

    expect(mockedPost).toHaveBeenCalledWith('/order/return/qc', {
      returnId: '71001',
      results: [
        {
          lineId: '72001',
          passedQty: 2,
          failedQty: 1,
          qcResult: 'PASS_WITH_REWORK',
        },
      ],
    });
  });

  it('queries return orders with normalized paging filters', async () => {
    mockedPost.mockResolvedValueOnce({ data: { code: 0, message: 'success', success: true, data: { records: [], total: 0 } } });

    await pageReturnOrders({ current: 0, size: 0, customerId: ' 20001 ', status: ' ' });

    expect(mockedPost).toHaveBeenCalledWith('/order/return/page', {
      current: 1,
      size: 1,
      customerId: '20001',
    });
  });
});
