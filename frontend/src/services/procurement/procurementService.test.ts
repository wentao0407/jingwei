import { beforeEach, describe, expect, it, vi } from 'vitest';
import {
  approveBom,
  calculateMrp,
  createAsn,
  createBom,
  createProcurementOrder,
  deleteBom,
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
  updateBom,
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

  it('creates procurement orders and ASN records', async () => {
    mockedPost
      .mockResolvedValueOnce({ data: ok({ id: '70002' }) })
      .mockResolvedValueOnce({ data: ok({ id: '81002' }) });

    await createProcurementOrder({
      supplierId: ' 90001 ',
      orderDate: ' 2026-05-18 ',
      expectedDeliveryDate: '',
      remark: ' 首批采购 ',
      lines: [
        {
          materialId: '30001',
          materialType: ' FABRIC ',
          quantity: 20,
          unit: ' 米 ',
          unitPrice: 12.5,
          mrpResultId: '',
          remark: '',
        },
      ],
    });
    await createAsn({
      procurementOrderId: ' 70002 ',
      supplierId: '90001',
      expectedArrivalDate: ' 2026-05-25 ',
      remark: '',
      lines: [
        {
          procurementLineId: '71001',
          materialId: '30001',
          expectedQuantity: 20,
          batchNo: ' BATCH-01 ',
          remark: '',
        },
      ],
    });

    expect(mockedPost).toHaveBeenNthCalledWith(1, '/procurement/order/create', {
      supplierId: '90001',
      orderDate: '2026-05-18',
      remark: '首批采购',
      lines: [
        {
          materialId: '30001',
          materialType: 'FABRIC',
          quantity: 20,
          unit: '米',
          unitPrice: 12.5,
        },
      ],
    });
    expect(mockedPost).toHaveBeenNthCalledWith(2, '/procurement/asn/create', {
      procurementOrderId: '70002',
      supplierId: '90001',
      expectedArrivalDate: '2026-05-25',
      lines: [
        {
          procurementLineId: '71001',
          materialId: '30001',
          expectedQuantity: 20,
          batchNo: 'BATCH-01',
        },
      ],
    });
  });

  it('creates updates and deletes BOM records', async () => {
    mockedPost
      .mockResolvedValueOnce({ data: ok({ id: '91002' }) })
      .mockResolvedValueOnce({ data: ok({ id: '91002' }) })
      .mockResolvedValueOnce({ data: ok(null) });

    const payload = {
      spuId: ' 80001 ',
      effectiveFrom: ' 2026-05-01 ',
      effectiveTo: '',
      remark: ' 基础 BOM ',
      items: [
        {
          materialId: '30001',
          materialType: ' FABRIC ',
          consumptionType: ' FIXED_PER_PIECE ',
          baseConsumption: 1.2,
          baseSizeId: '',
          unit: ' 米 ',
          wastageRate: 0.08,
          remark: '',
          sizeConsumptions: [
            { sizeId: '50001', code: ' S ', consumption: 1.1 },
          ],
        },
      ],
    };

    await createBom(payload);
    await updateBom(' 91002 ', payload);
    await deleteBom(' 91002 ');

    const normalizedPayload = {
      spuId: '80001',
      effectiveFrom: '2026-05-01',
      remark: '基础 BOM',
      items: [
        {
          materialId: '30001',
          materialType: 'FABRIC',
          consumptionType: 'FIXED_PER_PIECE',
          baseConsumption: 1.2,
          unit: '米',
          wastageRate: 0.08,
          sizeConsumptions: [{ sizeId: '50001', code: 'S', consumption: 1.1 }],
        },
      ],
    };
    expect(mockedPost).toHaveBeenNthCalledWith(1, '/procurement/bom/create', normalizedPayload);
    expect(mockedPost).toHaveBeenNthCalledWith(2, '/procurement/bom/update', normalizedPayload, {
      params: { bomId: '91002' },
    });
    expect(mockedPost).toHaveBeenNthCalledWith(3, '/procurement/bom/delete', null, {
      params: { bomId: '91002' },
    });
  });
});

function ok(data: unknown) {
  return { code: 0, message: 'success', success: true, data };
}
