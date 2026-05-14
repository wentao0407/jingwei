import { beforeEach, describe, expect, it, vi } from 'vitest';
import { pageInventoryLedger, pageOperationFlows, queryInventoryAge, queryTurnoverAnalysis } from './reportService';
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

describe('reportService', () => {
  beforeEach(() => {
    mockedPost.mockReset();
  });

  it('queries report endpoints with normalized filters', async () => {
    mockedPost
      .mockResolvedValueOnce({ data: ok({ records: [], total: 0 }) })
      .mockResolvedValueOnce({ data: ok({ records: [], total: 0 }) })
      .mockResolvedValueOnce({ data: ok({ totalCount: 0, details: [] }) })
      .mockResolvedValueOnce({ data: ok({ records: [], total: 0 }) });

    await pageInventoryLedger({ current: 0, size: 0, inventoryType: ' SKU ', warehouseId: '' });
    await pageOperationFlows({ current: 0, size: 0, inventoryType: ' MATERIAL ', operationNo: ' OP-1 ' });
    await queryInventoryAge({ current: 0, size: 0, keyword: ' 春季 ' });
    await queryTurnoverAnalysis({ current: 0, size: 0, startDate: '2026-05-01', endDate: '' });

    expect(mockedPost).toHaveBeenNthCalledWith(1, '/report/ledger/page', {
      current: 1,
      size: 1,
      inventoryType: 'SKU',
    });
    expect(mockedPost).toHaveBeenNthCalledWith(2, '/report/flow/page', {
      current: 1,
      size: 1,
      inventoryType: 'MATERIAL',
      operationNo: 'OP-1',
    });
    expect(mockedPost).toHaveBeenNthCalledWith(3, '/report/age/summary', {
      current: 1,
      size: 1,
      keyword: '春季',
    });
    expect(mockedPost).toHaveBeenNthCalledWith(4, '/report/turnover/page', {
      current: 1,
      size: 1,
      startDate: '2026-05-01',
    });
  });
});

function ok(data: unknown) {
  return { code: 0, message: 'success', success: true, data };
}
