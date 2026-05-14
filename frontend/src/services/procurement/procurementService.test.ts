import { beforeEach, describe, expect, it, vi } from 'vitest';
import {
  approveBom,
  calculateMrp,
  fireProcurementOrderEvent,
  getAsnDetail,
  getBomDetail,
  getProcurementOrderAvailableActions,
  getProcurementOrderDetail,
  pageAsns,
  pageBoms,
  pageMrpResults,
  pageProcurementOrders,
  receiveAsnGoods,
  submitAsnQc,
} from './procurementService';
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

describe('procurementService', () => {
  beforeEach(() => {
    mockedPost.mockReset();
  });

  it('queries procurement orders and fires order events', async () => {
    mockedPost
      .mockResolvedValueOnce({ data: ok({ records: [], total: 0 }) })
      .mockResolvedValueOnce({ data: ok({ id: '70001' }) })
      .mockResolvedValueOnce({ data: ok(['SUBMIT', 'APPROVE']) })
      .mockResolvedValueOnce({ data: ok(null) });

    await pageProcurementOrders({ current: 0, size: 0, supplierId: ' 90001 ', status: '' });
    await getProcurementOrderDetail('70001');
    await getProcurementOrderAvailableActions('70001');
    await fireProcurementOrderEvent({ orderId: '70001', event: ' SUBMIT ' });

    expect(mockedPost).toHaveBeenNthCalledWith(1, '/procurement/order/page', {
      current: 1,
      size: 1,
      supplierId: '90001',
    });
    expect(mockedPost).toHaveBeenNthCalledWith(2, '/procurement/order/detail', null, {
      params: { orderId: '70001' },
    });
    expect(mockedPost).toHaveBeenNthCalledWith(3, '/procurement/order/available-actions', null, {
      params: { orderId: '70001' },
    });
    expect(mockedPost).toHaveBeenNthCalledWith(4, '/procurement/order/fire-event', {
      orderId: '70001',
      event: 'SUBMIT',
    });
  });

  it('queries ASN records and submits receive and qc payloads', async () => {
    mockedPost
      .mockResolvedValueOnce({ data: ok({ records: [], total: 0 }) })
      .mockResolvedValueOnce({ data: ok({ id: '81001' }) })
      .mockResolvedValueOnce({ data: ok(null) })
      .mockResolvedValueOnce({ data: ok(null) });

    await pageAsns({ current: 0, size: 0, procurementOrderId: ' 70001 ', status: '' });
    await getAsnDetail('81001');
    await receiveAsnGoods({ asnId: '81001', lines: [{ lineId: '82001', receivedQuantity: 12 }] });
    await submitAsnQc({
      lineId: '82001',
      acceptedQuantity: 11,
      rejectedQuantity: 1,
      inspector: ' QA ',
      conclusion: ' 合格 ',
    });

    expect(mockedPost).toHaveBeenNthCalledWith(1, '/procurement/asn/page', {
      current: 1,
      size: 1,
      procurementOrderId: '70001',
    });
    expect(mockedPost).toHaveBeenNthCalledWith(2, '/procurement/asn/detail', null, {
      params: { asnId: '81001' },
    });
    expect(mockedPost).toHaveBeenNthCalledWith(3, '/procurement/asn/receive', {
      asnId: '81001',
      lines: [{ lineId: '82001', receivedQuantity: 12 }],
    });
    expect(mockedPost).toHaveBeenNthCalledWith(4, '/procurement/asn/qc', {
      lineId: '82001',
      acceptedQuantity: 11,
      rejectedQuantity: 1,
      inspector: 'QA',
      conclusion: '合格',
    });
  });

  it('queries BOM and MRP endpoints', async () => {
    mockedPost
      .mockResolvedValueOnce({ data: ok({ records: [], total: 0 }) })
      .mockResolvedValueOnce({ data: ok({ id: '91001' }) })
      .mockResolvedValueOnce({ data: ok(null) })
      .mockResolvedValueOnce({ data: ok({ batchNo: 'MRP-001', totalItems: 2 }) })
      .mockResolvedValueOnce({ data: ok({ records: [], total: 0 }) });

    await pageBoms({ current: 0, size: 0, spuId: ' 80001 ', status: '' });
    await getBomDetail('91001');
    await approveBom('91001');
    await calculateMrp({ productionOrderIds: [' 50001 ', ''] });
    await pageMrpResults({ current: 0, size: 0, batchNo: ' MRP-001 ', status: '' });

    expect(mockedPost).toHaveBeenNthCalledWith(1, '/procurement/bom/page', {
      current: 1,
      size: 1,
      spuId: '80001',
    });
    expect(mockedPost).toHaveBeenNthCalledWith(2, '/procurement/bom/detail', null, {
      params: { bomId: '91001' },
    });
    expect(mockedPost).toHaveBeenNthCalledWith(3, '/procurement/bom/approve', null, {
      params: { bomId: '91001' },
    });
    expect(mockedPost).toHaveBeenNthCalledWith(4, '/procurement/mrp/calculate', {
      productionOrderIds: ['50001'],
    });
    expect(mockedPost).toHaveBeenNthCalledWith(5, '/procurement/mrp/results', {
      current: 1,
      size: 1,
      batchNo: 'MRP-001',
    });
  });
});

function ok(data: unknown) {
  return { code: 0, message: 'success', success: true, data };
}
