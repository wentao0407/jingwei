import { beforeEach, describe, expect, it, vi } from 'vitest';
import {
  confirmPutaway,
  confirmReceive,
  createReceivingFromAsn,
  getReceivingDetail,
  suggestReceivingLocations,
} from './receivingService';
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

describe('receivingService', () => {
  beforeEach(() => {
    mockedPost.mockReset();
  });

  it('creates receiving orders and confirms receiving lines', async () => {
    mockedPost
      .mockResolvedValueOnce({ data: ok({ id: '10001' }) })
      .mockResolvedValueOnce({ data: ok(null) })
      .mockResolvedValueOnce({ data: ok({ id: '10001' }) });

    await createReceivingFromAsn({ asnId: ' 81001 ', warehouseId: ' 30001 ', dockNo: ' A-01 ' });
    await confirmReceive({ receivingLineId: '10002', receivedQty: 12, rollCount: 2 });
    await getReceivingDetail('10001');

    expect(mockedPost).toHaveBeenNthCalledWith(1, '/warehouse/receiving/create', {
      asnId: '81001',
      warehouseId: '30001',
      dockNo: 'A-01',
    });
    expect(mockedPost).toHaveBeenNthCalledWith(2, '/warehouse/receiving/confirm', {
      receivingLineId: '10002',
      receivedQty: 12,
      rollCount: 2,
    });
    expect(mockedPost).toHaveBeenNthCalledWith(3, '/warehouse/receiving/detail', null, {
      params: { receivingId: '10001' },
    });
  });

  it('suggests locations and confirms putaway lines', async () => {
    mockedPost
      .mockResolvedValueOnce({ data: ok([{ locationId: '50001', fullCode: 'A-01-01' }]) })
      .mockResolvedValueOnce({ data: ok(null) });

    await suggestReceivingLocations('10002');
    await confirmPutaway({ receivingLineId: '10002', locationId: '50001' });

    expect(mockedPost).toHaveBeenNthCalledWith(1, '/warehouse/receiving/suggest-locations', null, {
      params: { receivingLineId: '10002' },
    });
    expect(mockedPost).toHaveBeenNthCalledWith(2, '/warehouse/receiving/putaway', {
      receivingLineId: '10002',
      locationId: '50001',
    });
  });
});

function ok(data: unknown) {
  return { code: 0, message: 'success', success: true, data };
}
