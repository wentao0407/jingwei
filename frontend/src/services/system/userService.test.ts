import { beforeEach, describe, expect, it, vi } from 'vitest';
import { apiClient } from '@/services/http/apiClient';
import {
  assignUserRoles,
  changeUserPassword,
  createUser,
  deactivateUser,
  getUserDetail,
  listUsers,
  updateUser,
} from './userService';

describe('userService', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

  it('lists users with normalized paging and filters', async () => {
    const postSpy = vi.spyOn(apiClient, 'post').mockResolvedValue({
      data: ok({ records: [], total: 0, current: 1, size: 1, pages: 0 }),
    });

    await listUsers({ current: 0, size: 0, keyword: ' admin ', status: '' });

    expect(postSpy).toHaveBeenCalledWith('/system/user/page', {
      current: 1,
      size: 1,
      keyword: 'admin',
    });
  });

  it('creates users with trimmed optional fields', async () => {
    const postSpy = vi.spyOn(apiClient, 'post').mockResolvedValue({
      data: ok({
        id: '2051932034979037191',
        username: 'operator',
        status: 'ACTIVE',
        roleIds: [],
      }),
    });

    await createUser({
      username: ' operator ',
      password: ' Secure123 ',
      realName: ' 运营员 ',
      phone: '',
      email: undefined,
    });

    expect(postSpy).toHaveBeenCalledWith('/system/user/create', {
      username: 'operator',
      password: 'Secure123',
      realName: '运营员',
    });
  });

  it('keeps snowflake ids as strings for update, deactivate and role assignment', async () => {
    const userId = '2051932034979037191';
    const postSpy = vi.spyOn(apiClient, 'post').mockResolvedValue({
      data: ok({
        id: userId,
        username: 'operator',
        status: 'ACTIVE',
        roleIds: ['2051932034979037192'],
      }),
    });

    await updateUser(userId, { realName: ' 运营主管 ', email: '' });
    await deactivateUser(userId);
    await assignUserRoles(userId, { roleIds: ['2051932034979037192'] });

    expect(postSpy).toHaveBeenNthCalledWith(
      1,
      '/system/user/update',
      { realName: '运营主管' },
      { params: { userId } },
    );
    expect(postSpy).toHaveBeenNthCalledWith(2, '/system/user/deactivate', null, {
      params: { userId },
    });
    expect(postSpy).toHaveBeenNthCalledWith(
      3,
      '/system/user/assignRoles',
      { roleIds: ['2051932034979037192'] },
      { params: { userId } },
    );
  });

  it('loads user detail and changes password by query parameter', async () => {
    const userId = '2051932034979037191';
    const postSpy = vi.spyOn(apiClient, 'post').mockResolvedValue({
      data: ok({ id: userId, username: 'operator', status: 'ACTIVE', roleIds: [] }),
    });

    await getUserDetail(` ${userId} `);
    await changeUserPassword(` ${userId} `, {
      oldPassword: ' OldPass123 ',
      newPassword: ' NewPass123 ',
    });

    expect(postSpy).toHaveBeenNthCalledWith(1, '/system/user/detail', null, {
      params: { userId },
    });
    expect(postSpy).toHaveBeenNthCalledWith(
      2,
      '/system/user/changePassword',
      { oldPassword: 'OldPass123', newPassword: 'NewPass123' },
      { params: { userId } },
    );
  });
});

function ok(data: unknown) {
  return { code: 0, message: 'success', success: true, data };
}
