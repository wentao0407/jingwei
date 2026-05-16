import { beforeEach, describe, expect, it, vi } from 'vitest';
import { apiClient } from '@/services/http/apiClient';
import { getCurrentUserPermissions, login } from './authService';

describe('authService', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

  it('logs in with the provided credentials and unwraps the session', async () => {
    const session = {
      token: 'jwt-token',
      userId: '2051932034979037191',
      username: 'admin',
      realName: '系统管理员',
      roleIds: ['1'],
      permissions: ['system:user:view'],
      menuTree: [],
      passwordExpired: false,
    };
    const postSpy = vi.spyOn(apiClient, 'post').mockResolvedValue({
      data: ok(session),
    });

    await expect(login({ username: 'admin', password: 'admin123' })).resolves.toEqual(session);
    expect(postSpy).toHaveBeenCalledWith('/auth/login', {
      username: 'admin',
      password: 'admin123',
    });
  });

  it('loads current user permissions', async () => {
    const permissions = {
      menuTree: [],
      permissions: ['report:ledger:view'],
    };
    const postSpy = vi.spyOn(apiClient, 'post').mockResolvedValue({
      data: ok(permissions),
    });

    await expect(getCurrentUserPermissions()).resolves.toEqual(permissions);
    expect(postSpy).toHaveBeenCalledWith('/system/menu/permissions');
  });
});

function ok(data: unknown) {
  return { code: 0, message: 'success', success: true, data };
}
