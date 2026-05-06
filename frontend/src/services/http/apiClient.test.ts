import { describe, expect, it, vi, beforeEach } from 'vitest';
import { apiClient, getApiErrorMessage } from './apiClient';
import { onUnauthorized } from '@/shared/auth/authEvents';
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

  it('clears token and emits unauthorized event when backend rejects the session', async () => {
    setAccessToken('expired-token');
    const listener = vi.fn();
    const unsubscribe = onUnauthorized(listener);
    const adapter = vi.fn().mockRejectedValue({
      response: {
        status: 401,
        data: {
          code: 10005,
          message: '未授权，请先登录',
          data: null,
          success: false,
        },
      },
    });

    await expect(apiClient.get('/system/profile', { adapter })).rejects.toBeDefined();

    unsubscribe();
    expect(localStorage.getItem('jingwei.accessToken')).toBeNull();
    expect(listener).toHaveBeenCalledTimes(1);
  });
});
