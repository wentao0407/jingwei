import { beforeEach, describe, expect, it, vi } from 'vitest';
import {
  fireProductionLineEvent,
  fireProductionOrderEvent,
  getProductionLineAvailableActions,
  getProductionOrderAvailableActions,
  getProductionOrderDetail,
  pageProductionOrders,
} from './productionOrderService';
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

describe('productionOrderService', () => {
  beforeEach(() => {
    mockedPost.mockReset();
  });

  it('queries production orders with normalized filters', async () => {
    mockedPost.mockResolvedValueOnce({
      data: { code: 0, message: 'success', success: true, data: { records: [], total: 0 } },
    });

    await pageProductionOrders({
      current: 0,
      size: 0,
      orderNo: ' MO ',
      status: '',
      planDateStart: ' 2026-05-01 ',
      planDateEnd: '',
    });

    expect(mockedPost).toHaveBeenCalledWith('/order/production/page', {
      current: 1,
      size: 1,
      orderNo: 'MO',
      planDateStart: '2026-05-01',
    });
  });

  it('loads detail and available actions by query parameters', async () => {
    mockedPost
      .mockResolvedValueOnce({ data: { code: 0, message: 'success', success: true, data: { id: '50001' } } })
      .mockResolvedValueOnce({ data: { code: 0, message: 'success', success: true, data: [{ event: 'RELEASE', label: '下达生产订单' }] } })
      .mockResolvedValueOnce({ data: { code: 0, message: 'success', success: true, data: [{ event: 'START_CUTTING', label: '开始裁剪' }] } });

    await getProductionOrderDetail('50001');
    await getProductionOrderAvailableActions('50001');
    await getProductionLineAvailableActions('50001', '60001');

    expect(mockedPost).toHaveBeenNthCalledWith(1, '/order/production/detail', null, { params: { orderId: '50001' } });
    expect(mockedPost).toHaveBeenNthCalledWith(2, '/order/production/available-actions', null, { params: { orderId: '50001' } });
    expect(mockedPost).toHaveBeenNthCalledWith(3, '/order/production/line-available-actions', null, {
      params: { orderId: '50001', lineId: '60001' },
    });
  });

  it('fires order and line events', async () => {
    mockedPost
      .mockResolvedValueOnce({ data: { code: 0, message: 'success', success: true, data: null } })
      .mockResolvedValueOnce({ data: { code: 0, message: 'success', success: true, data: null } });

    await fireProductionOrderEvent({ orderId: '50001', event: ' RELEASE ' });
    await fireProductionLineEvent({ orderId: '50001', lineId: '60001', event: ' START_CUTTING ' });

    expect(mockedPost).toHaveBeenNthCalledWith(1, '/order/production/fire-event', {
      orderId: '50001',
      event: 'RELEASE',
    });
    expect(mockedPost).toHaveBeenNthCalledWith(2, '/order/production/fire-line-event', {
      orderId: '50001',
      lineId: '60001',
      event: 'START_CUTTING',
    });
  });
});
