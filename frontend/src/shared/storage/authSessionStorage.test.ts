import { beforeEach, describe, expect, it } from 'vitest';
import { clearAuthSession, getAuthSession, setAuthSession } from './authSessionStorage';

describe('authSessionStorage', () => {
  beforeEach(() => {
    window.localStorage.clear();
  });

  it('stores and reads user session with permissions and menu tree', () => {
    setAuthSession({
      userId: '1',
      username: 'admin',
      realName: '系统管理员',
      roleIds: ['1'],
      permissions: ['system:user:create'],
      menuTree: [
        {
          id: '10',
          parentId: '0',
          name: '系统管理',
          type: 'DIRECTORY',
          path: '/system',
          icon: 'SettingOutlined',
          sortOrder: 1,
          visible: true,
          status: 'ACTIVE',
          children: [],
        },
      ],
    });

    expect(getAuthSession()).toEqual(
      expect.objectContaining({
        realName: '系统管理员',
        permissions: ['system:user:create'],
        menuTree: [expect.objectContaining({ name: '系统管理' })],
      }),
    );
  });

  it('clears stored user session', () => {
    setAuthSession({
      userId: '1',
      username: 'admin',
      realName: '系统管理员',
      roleIds: ['1'],
      permissions: [],
      menuTree: [],
    });

    clearAuthSession();

    expect(getAuthSession()).toBeNull();
  });
});
