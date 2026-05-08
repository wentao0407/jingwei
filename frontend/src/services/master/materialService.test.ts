import { beforeEach, describe, expect, it, vi } from 'vitest';
import { apiClient } from '@/services/http/apiClient';
import {
  createMaterial,
  deactivateMaterial,
  getMaterialAttributeDefs,
  listMaterials,
  updateMaterial,
} from './materialService';

describe('materialService', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

  it('loads paged materials with normalized query', async () => {
    const postSpy = vi.spyOn(apiClient, 'post').mockResolvedValue({
      data: {
        code: 0,
        message: 'success',
        success: true,
        data: { records: [], total: 0, current: 1, size: 10, pages: 0 },
      },
    });

    await listMaterials({
      current: 0,
      size: 10,
      keyword: ' 棉 ',
      type: 'FABRIC',
      categoryId: '',
      status: 'ACTIVE',
    });

    expect(postSpy).toHaveBeenCalledWith('/master/material/page', {
      current: 1,
      size: 10,
      keyword: '棉',
      type: 'FABRIC',
      status: 'ACTIVE',
    });
  });

  it('creates materials with extAttrs and trimmed optional fields', async () => {
    const postSpy = vi.spyOn(apiClient, 'post').mockResolvedValue({
      data: { code: 0, message: 'success', success: true, data: { id: '1', code: 'MAT-000001' } },
    });

    await createMaterial({
      name: ' 40支棉布 ',
      type: 'FABRIC',
      categoryId: '271',
      unit: ' 米 ',
      extAttrs: { width: '150cm' },
      remark: '',
    });

    expect(postSpy).toHaveBeenCalledWith('/master/material/create', {
      name: '40支棉布',
      type: 'FABRIC',
      categoryId: '271',
      unit: '米',
      extAttrs: { width: '150cm' },
    });
  });

  it('updates and deactivates material by materialId query param', async () => {
    const postSpy = vi.spyOn(apiClient, 'post').mockResolvedValue({
      data: { code: 0, message: 'success', success: true, data: { id: '1', code: 'MAT-000001' } },
    });

    await updateMaterial('2051932034979037191', { name: ' 精梳棉 ', categoryId: '271', unit: '米' });
    await deactivateMaterial('2051932034979037191');

    expect(postSpy).toHaveBeenCalledWith(
      '/master/material/update',
      { name: '精梳棉', categoryId: '271', unit: '米' },
      { params: { materialId: '2051932034979037191' } },
    );
    expect(postSpy).toHaveBeenCalledWith('/master/material/deactivate', null, {
      params: { materialId: '2051932034979037191' },
    });
  });

  it('loads material attribute definitions by materialType query param', async () => {
    const postSpy = vi.spyOn(apiClient, 'post').mockResolvedValue({
      data: { code: 0, message: 'success', success: true, data: [] },
    });

    await getMaterialAttributeDefs('FABRIC');

    expect(postSpy).toHaveBeenCalledWith('/master/material/attributeDefs', null, {
      params: { materialType: 'FABRIC' },
    });
  });
});
