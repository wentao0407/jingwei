import { beforeEach, describe, expect, it, vi } from 'vitest';
import { confirmShipment } from './shipmentService';
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
});

function ok(data: unknown) {
  return { code: 0, message: 'success', success: true, data };
}
