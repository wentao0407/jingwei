import { beforeEach, describe, expect, it, vi } from 'vitest';
import { apiClient } from '@/services/http/apiClient';
import { createRole, updateRole } from './roleService';

describe('roleService', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

  it('creates roles with trimmed optional fields', async () => {
    const postSpy = vi.spyOn(apiClient, 'post').mockResolvedValue({
      data: {
        code: 0,
        message: 'success',
        success: true,
        data: {
          id: '2051932034979037191',
          roleCode: 'QUALITY_MANAGER',
          roleName: '质检主管',
          description: '负责质检流程',
          status: 'ACTIVE',
        },
      },
    });

    await createRole({
      roleCode: ' QUALITY_MANAGER ',
      roleName: ' 质检主管 ',
      description: ' 负责质检流程 ',
    });

    expect(postSpy).toHaveBeenCalledWith('/system/role/create', {
      roleCode: 'QUALITY_MANAGER',
      roleName: '质检主管',
      description: '负责质检流程',
    });
  });

  it('updates roles with roleId query param and skips empty fields', async () => {
    const postSpy = vi.spyOn(apiClient, 'post').mockResolvedValue({
      data: {
        code: 0,
        message: 'success',
        success: true,
        data: {
          id: '2051932034979037191',
          roleCode: 'QUALITY_MANAGER',
          roleName: '质检主管',
          status: 'INACTIVE',
        },
      },
    });

    await updateRole('2051932034979037191', {
      roleName: ' 质检主管 ',
      description: '',
      status: 'INACTIVE',
    });

    expect(postSpy).toHaveBeenCalledWith(
      '/system/role/update',
      {
        roleName: '质检主管',
        status: 'INACTIVE',
      },
      {
        params: { roleId: '2051932034979037191' },
      },
    );
  });
});
