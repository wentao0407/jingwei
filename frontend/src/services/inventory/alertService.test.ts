import { beforeEach, describe, expect, it, vi } from 'vitest';
import { acknowledgeAlert, listInventoryAlerts, scanInventoryAlerts } from './alertService';
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

describe('alertService', () => {
  beforeEach(() => {
    mockedPost.mockReset();
  });

  it('lists, scans and acknowledges inventory alerts', async () => {
    mockedPost
      .mockResolvedValueOnce({ data: ok([{ id: '1', status: 'ACTIVE' }]) })
      .mockResolvedValueOnce({ data: ok({ created: 2 }) })
      .mockResolvedValueOnce({ data: ok(null) });

    await listInventoryAlerts({ status: ' ACTIVE ' });
    await scanInventoryAlerts();
    await acknowledgeAlert('90001');

    expect(mockedPost).toHaveBeenNthCalledWith(1, '/inventory/alert/list', { status: 'ACTIVE' });
    expect(mockedPost).toHaveBeenNthCalledWith(2, '/inventory/alert/scan');
    expect(mockedPost).toHaveBeenNthCalledWith(3, '/inventory/alert/acknowledge', null, {
      params: { alertId: '90001' },
    });
  });
});

function ok(data: unknown) {
  return { code: 0, message: 'success', success: true, data };
}
