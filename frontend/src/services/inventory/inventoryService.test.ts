import { beforeEach, describe, expect, it, vi } from 'vitest';
import {
  confirmInbound,
  confirmOutbound,
  createInboundOrder,
  createOutboundOrder,
  createStocktakingOrder,
  getInboundDetail,
  getOutboundDetail,
  getStocktakingDetail,
  pageInboundOrders,
  pageOutboundOrders,
  pageStocktakingOrders,
  recordStocktakingCount,
  reviewStocktaking,
  startStocktaking,
  submitStocktaking,
} from './inventoryService';
import { apiClient } from '@/services/http/apiClient';

vi.mock('@/services/http/apiClient', async () => {
  const actual = await vi.importActual<typeof import('@/services/http/apiClient')>('@/services/http/apiClient');
  return { ...actual, apiClient: { post: vi.fn() } };
});

const mockedPost = vi.mocked(apiClient.post);

describe('inventoryService', () => {
  beforeEach(() => mockedPost.mockReset());

  it('queries and confirms inbound orders', async () => {
    mockedPost.mockResolvedValue({ data: ok({ records: [], total: 0 }) });

    await pageInboundOrders({ current: 0, size: 0, warehouseId: ' 30001 ', inboundNo: ' IN-01 ', status: '' });
    await getInboundDetail('10001');
    await confirmInbound('10001');
    await createInboundOrder({
      inboundType: 'PURCHASE',
      warehouseId: '30001',
      lines: [{ inventoryType: 'MATERIAL', materialId: '80001', plannedQty: 10, actualQty: 10 }],
    });

    expect(mockedPost).toHaveBeenNthCalledWith(1, '/inventory/inbound/page', {
      current: 1,
      size: 1,
      warehouseId: '30001',
      inboundNo: 'IN-01',
    });
    expect(mockedPost).toHaveBeenNthCalledWith(2, '/inventory/inbound/detail', null, { params: { inboundId: '10001' } });
    expect(mockedPost).toHaveBeenNthCalledWith(3, '/inventory/inbound/confirm', null, { params: { inboundId: '10001' } });
    expect(mockedPost).toHaveBeenNthCalledWith(4, '/inventory/inbound/create', expect.objectContaining({ warehouseId: '30001' }));
  });

  it('queries and confirms outbound orders', async () => {
    mockedPost.mockResolvedValue({ data: ok({ records: [], total: 0 }) });

    await pageOutboundOrders({ current: 0, size: 0, warehouseId: ' 30001 ', outboundNo: ' OUT-01 ', status: '' });
    await getOutboundDetail('20001');
    await confirmOutbound('20001');
    await createOutboundOrder({
      outboundType: 'MATERIAL',
      warehouseId: '30001',
      lines: [{ inventoryType: 'MATERIAL', materialId: '80001', plannedQty: 5 }],
    });

    expect(mockedPost).toHaveBeenNthCalledWith(1, '/inventory/outbound/page', {
      current: 1,
      size: 1,
      warehouseId: '30001',
      outboundNo: 'OUT-01',
    });
    expect(mockedPost).toHaveBeenNthCalledWith(2, '/inventory/outbound/detail', null, { params: { outboundId: '20001' } });
    expect(mockedPost).toHaveBeenNthCalledWith(3, '/inventory/outbound/confirm', null, { params: { outboundId: '20001' } });
    expect(mockedPost).toHaveBeenNthCalledWith(4, '/inventory/outbound/create', expect.objectContaining({ warehouseId: '30001' }));
  });

  it('queries and operates stocktaking orders', async () => {
    mockedPost.mockResolvedValue({ data: ok({ records: [], total: 0 }) });

    await pageStocktakingOrders({ current: 0, size: 0, warehouseId: ' 30001 ', status: '' });
    await getStocktakingDetail('30001');
    await createStocktakingOrder({ stocktakingType: 'FULL', countMode: 'OPEN', warehouseId: '30001' });
    await startStocktaking('30001');
    await recordStocktakingCount({ stocktakingId: '30001', lineId: '31001', actualQty: 8 });
    await submitStocktaking('30001');
    await reviewStocktaking('30001');

    expect(mockedPost).toHaveBeenNthCalledWith(1, '/inventory/stocktaking/page', {
      current: 1,
      size: 1,
      warehouseId: '30001',
    });
    expect(mockedPost).toHaveBeenNthCalledWith(4, '/inventory/stocktaking/start', null, { params: { stocktakingId: '30001' } });
    expect(mockedPost).toHaveBeenNthCalledWith(5, '/inventory/stocktaking/record-count', {
      stocktakingId: '30001',
      lineId: '31001',
      actualQty: 8,
    });
  });
});

function ok(data: unknown) {
  return { code: 0, message: 'success', success: true, data };
}
