import { describe, expect, it, vi, beforeEach } from 'vitest';
import { apiClient, getApiErrorMessage } from './apiClient';
import { clearAccessToken, setAccessToken } from '@/shared/storage/tokenStorage';

describe('apiClient', () => {
  beforeEach(() => {
    clearAccessToken();
  });

  it('attaches bearer token when token exists', async () => {
    setAccessToken('jwt-token');
    const adapter = vi.fn().mockResolvedValue({
      data: { code: 0, message: 'success', data: null, success: true },
      status: 200,
      statusText: 'OK',
      headers: {},
      config: {},
    });

    await apiClient.get('/system/profile', { adapter });

    expect(adapter).toHaveBeenCalledWith(
      expect.objectContaining({
        headers: expect.objectContaining({
          Authorization: 'Bearer jwt-token',
        }),
      }),
    );
  });

  it('uses backend error message from response envelope', () => {
    const error = {
      response: {
        data: {
          code: 10005,
          message: '未授权，请先登录',
          data: null,
          success: false,
        },
      },
    };

    expect(getApiErrorMessage(error)).toBe('未授权，请先登录');
  });
});
