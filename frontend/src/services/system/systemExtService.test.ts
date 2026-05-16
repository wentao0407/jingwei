import { beforeEach, describe, expect, it, vi } from 'vitest';
import {
  configureDataScope,
  listAuditLogs,
  queryDataScope,
} from './systemExtService';
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

describe('systemExtService', () => {
  beforeEach(() => {
    mockedPost.mockReset();
  });

  it('queries audit logs with normalized filters', async () => {
    mockedPost.mockResolvedValueOnce({ data: ok({ records: [], total: 0 }) });

    await listAuditLogs({
      current: 0,
      size: 0,
      userId: ' 10001 ',
      module: ' ORDER ',
      operationType: '',
      startTime: ' 2026-05-01T00:00:00 ',
      endTime: '',
      keyword: ' 创建订单 ',
    });

    expect(mockedPost).toHaveBeenCalledWith('/system/audit-log/page', {
      current: 1,
      size: 1,
      userId: '10001',
      module: 'ORDER',
      startTime: '2026-05-01T00:00:00',
      keyword: '创建订单',
    });
  });

  it('queries and configures role data scopes', async () => {
    mockedPost
      .mockResolvedValueOnce({ data: ok([{ id: '1', roleId: '2', scopeType: 'WAREHOUSE', scopeValue: '1,2' }]) })
      .mockResolvedValueOnce({ data: ok(null) });

    await queryDataScope(' 2 ');
    await configureDataScope(' 2 ', {
      scopes: [
        { scopeType: ' WAREHOUSE ', scopeValue: ' 1,2 ' },
        { scopeType: '', scopeValue: 'ALL' },
      ],
    });

    expect(mockedPost).toHaveBeenNthCalledWith(1, '/system/data-scope/query', null, {
      params: { roleId: '2' },
    });
    expect(mockedPost).toHaveBeenNthCalledWith(
      2,
      '/system/data-scope/configure',
      { scopes: [{ scopeType: 'WAREHOUSE', scopeValue: '1,2' }] },
      { params: { roleId: '2' } },
    );
  });
});

function ok(data: unknown) {
  return { code: 0, message: 'success', success: true, data };
}
