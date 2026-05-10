import { beforeEach, describe, expect, it, vi } from 'vitest';
import {
  cancelSalesOrder,
  convertSalesOrderToProduction,
  createSalesOrder,
  createQuantityChange,
  deleteSalesOrder,
  getSalesOrderDetail,
  pageSalesOrders,
  resubmitSalesOrder,
  submitSalesOrder,
  updateSalesOrder,
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

  it('creates and updates sales orders with normalized lines', async () => {
    mockedPost
      .mockResolvedValueOnce({ data: { code: 0, message: 'success', success: true, data: { id: '10001' } } })
      .mockResolvedValueOnce({ data: { code: 0, message: 'success', success: true, data: { id: '10001' } } });

    const payload = {
      customerId: ' 20001 ',
      seasonId: '',
      orderDate: ' 2026-05-10 ',
      deliveryDate: '',
      remark: ' 首批订单 ',
      lines: [
        {
          spuId: '80001',
          colorWayId: '70001',
          sizeGroupId: '60001',
          sizes: [
            { sizeId: '50001', code: ' S ', quantity: 20 },
            { sizeId: '50002', code: 'M', quantity: 0 },
          ],
          unitPrice: 129,
          discountRate: 0.95,
          deliveryDate: '',
          remark: ' 黑色 ',
        },
      ],
    };

    await createSalesOrder(payload);
    await updateSalesOrder('10001', payload);

    expect(mockedPost).toHaveBeenNthCalledWith(1, '/order/sales/create', {
      customerId: '20001',
      orderDate: '2026-05-10',
      remark: '首批订单',
      lines: [
        {
          spuId: '80001',
          colorWayId: '70001',
          sizeGroupId: '60001',
          sizes: [
            { sizeId: '50001', code: 'S', quantity: 20 },
            { sizeId: '50002', code: 'M', quantity: 0 },
          ],
          unitPrice: 129,
          discountRate: 0.95,
          remark: '黑色',
        },
      ],
    });
    expect(mockedPost).toHaveBeenNthCalledWith(2, '/order/sales/update', {
      customerId: '20001',
      orderDate: '2026-05-10',
      remark: '首批订单',
      lines: [
        {
          spuId: '80001',
          colorWayId: '70001',
          sizeGroupId: '60001',
          sizes: [
            { sizeId: '50001', code: 'S', quantity: 20 },
            { sizeId: '50002', code: 'M', quantity: 0 },
          ],
          unitPrice: 129,
          discountRate: 0.95,
          remark: '黑色',
        },
      ],
    }, { params: { orderId: '10001' } });
  });

  it('converts confirmed sales orders and creates quantity changes', async () => {
    mockedPost
      .mockResolvedValueOnce({ data: { code: 0, message: 'success', success: true, data: { salesOrderId: '10003' } } })
      .mockResolvedValueOnce({ data: { code: 0, message: 'success', success: true, data: { id: '70001' } } });

    await convertSalesOrderToProduction({
      salesOrderId: '10003',
      lines: [
        { salesOrderLineId: '90003', skipCutting: false },
        { salesOrderLineId: '90004', skipCutting: true },
      ],
      deadlineDate: ' 2026-07-15 ',
      remark: ' 春季订单排产 ',
    });
    await createQuantityChange({
      orderId: '10003',
      orderLineId: '90003',
      sizeGroupId: '60001',
      sizes: [
        { sizeId: '50001', code: ' S ', quantity: 50 },
        { sizeId: '50002', code: 'M', quantity: 95 },
      ],
      reason: ' 客户追加数量 ',
    });

    expect(mockedPost).toHaveBeenNthCalledWith(1, '/order/sales/convert-to-production', {
      salesOrderId: '10003',
      lines: [
        { salesOrderLineId: '90003', skipCutting: false },
        { salesOrderLineId: '90004', skipCutting: true },
      ],
      deadlineDate: '2026-07-15',
      remark: '春季订单排产',
    });
    expect(mockedPost).toHaveBeenNthCalledWith(2, '/order/sales/quantity-change', {
      orderId: '10003',
      orderLineId: '90003',
      sizeGroupId: '60001',
      sizes: [
        { sizeId: '50001', code: 'S', quantity: 50 },
        { sizeId: '50002', code: 'M', quantity: 95 },
      ],
      reason: '客户追加数量',
    });
  });
});
