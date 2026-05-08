import { beforeEach, describe, expect, it, vi } from 'vitest';
import { apiClient } from '@/services/http/apiClient';
import { assignMenuPermissions, createMenu, deleteMenu, getRoleMenuIds, listMenus, updateMenu } from './menuService';

describe('menuService', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

  it('loads the full menu tree', async () => {
    const postSpy = vi.spyOn(apiClient, 'post').mockResolvedValue({
      data: {
        code: 0,
        message: 'success',
        success: true,
        data: [
          {
            id: '100',
            parentId: '0',
            name: '系统管理',
            type: 'DIRECTORY',
            path: '/system',
            children: [],
          },
        ],
      },
    });

    const result = await listMenus();

    expect(postSpy).toHaveBeenCalledWith('/system/menu/tree');
    expect(result).toHaveLength(1);
  });

  it('creates menus with trimmed optional fields', async () => {
    const postSpy = vi.spyOn(apiClient, 'post').mockResolvedValue({
      data: {
        code: 0,
        message: 'success',
        success: true,
        data: { id: '130', parentId: '100', name: '菜单管理', type: 'MENU' },
      },
    });

    await createMenu({
      parentId: '100',
      name: ' 菜单管理 ',
      type: 'MENU',
      path: ' /system/menu ',
      component: ' system/MenuList ',
      permission: '',
      icon: ' MenuOutlined ',
      sortOrder: 3,
      visible: true,
    });

    expect(postSpy).toHaveBeenCalledWith('/system/menu/create', {
      parentId: '100',
      name: '菜单管理',
      type: 'MENU',
      path: '/system/menu',
      component: 'system/MenuList',
      icon: 'MenuOutlined',
      sortOrder: 3,
      visible: true,
    });
  });

  it('updates menus with menuId query param and skips empty fields', async () => {
    const postSpy = vi.spyOn(apiClient, 'post').mockResolvedValue({
      data: {
        code: 0,
        message: 'success',
        success: true,
        data: { id: '130', parentId: '100', name: '菜单管理', type: 'MENU' },
      },
    });

    await updateMenu('130', {
      parentId: '100',
      name: ' 菜单管理 ',
      path: '',
      status: 'ACTIVE',
      visible: true,
    });

    expect(postSpy).toHaveBeenCalledWith(
      '/system/menu/update',
      {
        parentId: '100',
        name: '菜单管理',
        status: 'ACTIVE',
        visible: true,
      },
      {
        params: { menuId: '130' },
      },
    );
  });

  it('deletes menus with menuId query param', async () => {
    const postSpy = vi.spyOn(apiClient, 'post').mockResolvedValue({
      data: {
        code: 0,
        message: 'success',
        success: true,
        data: null,
      },
    });

    await deleteMenu('130');

    expect(postSpy).toHaveBeenCalledWith('/system/menu/delete', null, {
      params: { menuId: '130' },
    });
  });

  it('loads assigned menu ids by role id', async () => {
    const postSpy = vi.spyOn(apiClient, 'post').mockResolvedValue({
      data: {
        code: 0,
        message: 'success',
        success: true,
        data: ['100', '120', '121'],
      },
    });

    const result = await getRoleMenuIds('2051932034979037191');

    expect(postSpy).toHaveBeenCalledWith('/system/menu/roleMenuIds', null, {
      params: { roleId: '2051932034979037191' },
    });
    expect(result).toEqual(['100', '120', '121']);
  });

  it('assigns menu permissions to a role with string ids', async () => {
    const postSpy = vi.spyOn(apiClient, 'post').mockResolvedValue({
      data: {
        code: 0,
        message: 'success',
        success: true,
        data: null,
      },
    });

    await assignMenuPermissions({
      roleId: '2051932034979037191',
      menuIds: ['100', '120', '121'],
    });

    expect(postSpy).toHaveBeenCalledWith('/system/menu/assign', {
      roleId: '2051932034979037191',
      menuIds: ['100', '120', '121'],
    });
  });
});
