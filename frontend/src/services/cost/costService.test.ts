import { beforeEach, describe, expect, it, vi } from 'vitest';
import { getCostDetail, getCostIssueDetails } from './costService';
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

describe('costService', () => {
  beforeEach(() => {
    mockedPost.mockReset();
  });

  it('queries cost detail and material issue details', async () => {
    mockedPost
      .mockResolvedValueOnce({ data: ok({ id: '1' }) })
      .mockResolvedValueOnce({ data: ok([]) });

    await getCostDetail({ productionOrderId: ' 50001 ', productionLineId: ' 51001 ' });
    await getCostIssueDetails(' 50001 ');

    expect(mockedPost).toHaveBeenNthCalledWith(1, '/cost/detail', null, {
      params: { productionOrderId: '50001', productionLineId: '51001' },
    });
    expect(mockedPost).toHaveBeenNthCalledWith(2, '/cost/issues', null, {
      params: { productionOrderId: '50001' },
    });
  });
});

function ok(data: unknown) {
  return { code: 0, message: 'success', success: true, data };
}
