import { beforeEach, describe, expect, it, vi } from 'vitest';
import { apiClient } from '@/services/http/apiClient';
import { createCategory, deleteCategory, listCategoryTree, updateCategory } from './categoryService';

describe('categoryService', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

  it('loads category tree', async () => {
    const postSpy = vi.spyOn(apiClient, 'post').mockResolvedValue({
      data: { code: 0, message: 'success', success: true, data: [] },
    });

    await listCategoryTree();

    expect(postSpy).toHaveBeenCalledWith('/master/category/tree');
  });

  it('creates categories with trimmed optional fields', async () => {
    const postSpy = vi.spyOn(apiClient, 'post').mockResolvedValue({
      data: { code: 0, message: 'success', success: true, data: { id: '270' } },
    });

    await createCategory({
      parentId: '',
      code: ' fabric ',
      name: ' 面料 ',
      sortOrder: 1,
    });

    expect(postSpy).toHaveBeenCalledWith('/master/category/create', {
      code: 'fabric',
      name: '面料',
      sortOrder: 1,
    });
  });

  it('updates and deletes categories by categoryId query param', async () => {
    const postSpy = vi.spyOn(apiClient, 'post').mockResolvedValue({
      data: { code: 0, message: 'success', success: true, data: { id: '271' } },
    });

    await updateCategory('271', { code: ' cotton ', name: ' 棉布 ', sortOrder: 2, status: 'ACTIVE' });
    await deleteCategory('271');

    expect(postSpy).toHaveBeenCalledWith(
      '/master/category/update',
      { code: 'cotton', name: '棉布', sortOrder: 2, status: 'ACTIVE' },
      { params: { categoryId: '271' } },
    );
    expect(postSpy).toHaveBeenCalledWith('/master/category/delete', null, { params: { categoryId: '271' } });
  });
});
