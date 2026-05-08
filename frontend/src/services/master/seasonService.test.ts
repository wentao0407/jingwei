import { beforeEach, describe, expect, it, vi } from 'vitest';
import {
  closeSeason,
  createSeason,
  createWave,
  deleteSeason,
  deleteWave,
  getSeasonDetail,
  listSeasons,
  updateSeason,
  updateWave,
} from './seasonService';
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

describe('seasonService', () => {
  beforeEach(() => {
    mockedPost.mockReset();
  });

  it('lists seasons and gets detail with normalized params', async () => {
    mockedPost
      .mockResolvedValueOnce({ data: { code: 0, message: 'success', success: true, data: [] } })
      .mockResolvedValueOnce({ data: { code: 0, message: 'success', success: true, data: { id: '10001' } } });

    await listSeasons({ year: 2026, seasonType: ' SPRING_SUMMER ', status: '' });
    await getSeasonDetail('10001');

    expect(mockedPost).toHaveBeenNthCalledWith(1, '/master/season/list', null, {
      params: { year: 2026, seasonType: 'SPRING_SUMMER' },
    });
    expect(mockedPost).toHaveBeenNthCalledWith(2, '/master/season/detail', null, {
      params: { seasonId: '10001' },
    });
  });

  it('creates, updates, closes and deletes seasons', async () => {
    mockedPost
      .mockResolvedValueOnce({ data: { code: 0, message: 'success', success: true, data: { id: '10001' } } })
      .mockResolvedValueOnce({ data: { code: 0, message: 'success', success: true, data: { id: '10001' } } })
      .mockResolvedValueOnce({ data: { code: 0, message: 'success', success: true, data: null } })
      .mockResolvedValueOnce({ data: { code: 0, message: 'success', success: true, data: null } });

    await createSeason({
      code: ' 2026SS ',
      name: ' 2026春夏 ',
      year: 2026,
      seasonType: 'SPRING_SUMMER',
      startDate: '2026-03-01',
      endDate: '2026-08-31',
    });
    await updateSeason('10001', { name: ' 春夏更新 ', startDate: '2026-03-15', endDate: '' });
    await closeSeason('10001');
    await deleteSeason('10001');

    expect(mockedPost).toHaveBeenNthCalledWith(1, '/master/season/create', {
      code: '2026SS',
      name: '2026春夏',
      year: 2026,
      seasonType: 'SPRING_SUMMER',
      startDate: '2026-03-01',
      endDate: '2026-08-31',
    });
    expect(mockedPost).toHaveBeenNthCalledWith(
      2,
      '/master/season/update',
      { name: '春夏更新', startDate: '2026-03-15' },
      { params: { seasonId: '10001' } },
    );
    expect(mockedPost).toHaveBeenNthCalledWith(3, '/master/season/close', null, {
      params: { seasonId: '10001' },
    });
    expect(mockedPost).toHaveBeenNthCalledWith(4, '/master/season/delete', null, {
      params: { seasonId: '10001' },
    });
  });

  it('creates, updates and deletes waves', async () => {
    mockedPost
      .mockResolvedValueOnce({ data: { code: 0, message: 'success', success: true, data: { id: '20001' } } })
      .mockResolvedValueOnce({ data: { code: 0, message: 'success', success: true, data: { id: '20001' } } })
      .mockResolvedValueOnce({ data: { code: 0, message: 'success', success: true, data: null } });

    await createWave('10001', { code: ' W1 ', name: ' 第一波 ', deliveryDate: '2026-04-01', sortOrder: 1 });
    await updateWave('20001', { name: ' 第一波更新 ', deliveryDate: '', sortOrder: 2 });
    await deleteWave('20001');

    expect(mockedPost).toHaveBeenNthCalledWith(
      1,
      '/master/season/wave/create',
      { code: 'W1', name: '第一波', deliveryDate: '2026-04-01', sortOrder: 1 },
      { params: { seasonId: '10001' } },
    );
    expect(mockedPost).toHaveBeenNthCalledWith(
      2,
      '/master/season/wave/update',
      { name: '第一波更新', sortOrder: 2 },
      { params: { waveId: '20001' } },
    );
    expect(mockedPost).toHaveBeenNthCalledWith(3, '/master/season/wave/delete', null, {
      params: { waveId: '20001' },
    });
  });
});
