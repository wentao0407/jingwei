import { beforeEach, describe, expect, it, vi } from 'vitest';
import { confirmShipment, getShipmentDetail, pageShipments } from './shipmentService';
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

describe('shipmentService', () => {
  beforeEach(() => {
    mockedPost.mockReset();
  });

  it('confirms shipment with normalized payload', async () => {
    mockedPost.mockResolvedValueOnce({ data: ok(null) });

    await confirmShipment({ outboundId: ' 80001 ', salesOrderId: '' });

    expect(mockedPost).toHaveBeenCalledWith('/warehouse/shipment/confirm', {
      outboundId: '80001',
    });
  });

  it('queries shipment page and detail through shipment aggregate endpoints', async () => {
    mockedPost
      .mockResolvedValueOnce({ data: { code: 0, message: 'success', success: true, data: { records: [], total: 0 } } })
      .mockResolvedValueOnce({ data: { code: 0, message: 'success', success: true, data: { id: '90001' } } });

    await pageShipments({ current: 1, size: 20, outboundNo: ' CK-001 ', warehouseId: '', status: ' SHIPPED ' });
    await getShipmentDetail('90001');

    expect(mockedPost).toHaveBeenNthCalledWith(1, '/warehouse/shipment/page', {
      current: 1,
      size: 20,
      outboundNo: 'CK-001',
      status: 'SHIPPED',
    });
    expect(mockedPost).toHaveBeenNthCalledWith(2, '/warehouse/shipment/detail', null, {
      params: { shipmentId: '90001' },
    });
  });
});

function ok(data: unknown) {
  return { code: 0, message: 'success', success: true, data };
}
