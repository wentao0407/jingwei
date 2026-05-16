import { beforeEach, describe, expect, it, vi } from 'vitest';
import { cancelWave, completePickList, confirmPick, createWave, getWaveDetail, pageWaves } from './waveService';
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

describe('waveService', () => {
  beforeEach(() => {
    mockedPost.mockReset();
  });

  it('creates waves and drives picking operations', async () => {
    mockedPost
      .mockResolvedValueOnce({ data: ok('70001') })
      .mockResolvedValueOnce({ data: ok(null) })
      .mockResolvedValueOnce({ data: ok(null) })
      .mockResolvedValueOnce({ data: ok(null) });

    await createWave({
      warehouseId: ' 30001 ',
      strategy: ' BY_CUSTOMER ',
      outboundOrderIds: [' 80001 ', '', '80002'],
      remark: ' 首波 ',
    });
    await confirmPick({ pickItemId: ' 71001 ', actualQty: 12 });
    await completePickList('72001');
    await cancelWave('70001');

    expect(mockedPost).toHaveBeenNthCalledWith(1, '/warehouse/wave/create', {
      warehouseId: '30001',
      strategy: 'BY_CUSTOMER',
      outboundOrderIds: ['80001', '80002'],
      remark: '首波',
    });
    expect(mockedPost).toHaveBeenNthCalledWith(2, '/warehouse/wave/confirm-pick', {
      pickItemId: '71001',
      actualQty: 12,
    });
    expect(mockedPost).toHaveBeenNthCalledWith(3, '/warehouse/wave/complete-pick-list', null, {
      params: { pickListId: '72001' },
    });
    expect(mockedPost).toHaveBeenNthCalledWith(4, '/warehouse/wave/cancel', null, {
      params: { waveId: '70001' },
    });
  });

  it('queries waves and loads detail with normalized filters', async () => {
    mockedPost.mockResolvedValue({ data: ok({ records: [], total: 0 }) });

    await pageWaves({ current: 0, size: 0, warehouseId: ' 30001 ', status: ' ', waveNo: ' WV-01 ' });
    await getWaveDetail(' 10001 ');

    expect(mockedPost).toHaveBeenNthCalledWith(1, '/warehouse/wave/page', {
      current: 1,
      size: 1,
      warehouseId: '30001',
      waveNo: 'WV-01',
    });
    expect(mockedPost).toHaveBeenNthCalledWith(2, '/warehouse/wave/detail', null, {
      params: { waveId: '10001' },
    });
  });
});

function ok(data: unknown) {
  return { code: 0, message: 'success', success: true, data };
}
