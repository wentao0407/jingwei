import { beforeEach, describe, expect, it, vi } from 'vitest';
import {
  createSize,
  createSizeGroup,
  deleteSize,
  deleteSizeGroup,
  getSizeGroupDetail,
  listSizeGroups,
  updateSize,
  updateSizeGroup,
} from './sizeGroupService';
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

describe('sizeGroupService', () => {
  beforeEach(() => {
    mockedPost.mockReset();
  });

  it('lists size groups with trimmed optional filters', async () => {
    mockedPost.mockResolvedValueOnce({ data: { code: 0, message: 'success', success: true, data: [] } });

    await listSizeGroups({ category: ' WOMEN ', status: '' });

    expect(mockedPost).toHaveBeenCalledWith('/master/size-group/list', null, {
      params: { category: 'WOMEN' },
    });
  });

  it('creates, updates and deletes size groups', async () => {
    mockedPost
      .mockResolvedValueOnce({ data: { code: 0, message: 'success', success: true, data: { id: '10001' } } })
      .mockResolvedValueOnce({ data: { code: 0, message: 'success', success: true, data: { id: '10001' } } })
      .mockResolvedValueOnce({ data: { code: 0, message: 'success', success: true, data: null } });

    await createSizeGroup({ code: ' WOMEN_NEW ', name: ' 新女装 ', category: 'WOMEN' });
    await updateSizeGroup('10001', { name: ' 女装更新 ', status: 'ACTIVE' });
    await deleteSizeGroup('10001');

    expect(mockedPost).toHaveBeenNthCalledWith(1, '/master/size-group/create', {
      code: 'WOMEN_NEW',
      name: '新女装',
      category: 'WOMEN',
    });
    expect(mockedPost).toHaveBeenNthCalledWith(
      2,
      '/master/size-group/update',
      { name: '女装更新', status: 'ACTIVE' },
      { params: { sizeGroupId: '10001' } },
    );
    expect(mockedPost).toHaveBeenNthCalledWith(3, '/master/size-group/delete', null, {
      params: { sizeGroupId: '10001' },
    });
  });

  it('gets detail and manages sizes', async () => {
    mockedPost
      .mockResolvedValueOnce({ data: { code: 0, message: 'success', success: true, data: { id: '10001', sizes: [] } } })
      .mockResolvedValueOnce({ data: { code: 0, message: 'success', success: true, data: { id: '10101' } } })
      .mockResolvedValueOnce({ data: { code: 0, message: 'success', success: true, data: { id: '10101' } } })
      .mockResolvedValueOnce({ data: { code: 0, message: 'success', success: true, data: null } });

    await getSizeGroupDetail('10001');
    await createSize('10001', { code: ' M ', name: ' 中码 ', sortOrder: 2 });
    await updateSize('10101', { name: ' 中码更新 ' });
    await deleteSize('10101');

    expect(mockedPost).toHaveBeenNthCalledWith(1, '/master/size-group/detail', null, {
      params: { sizeGroupId: '10001' },
    });
    expect(mockedPost).toHaveBeenNthCalledWith(
      2,
      '/master/size-group/size/create',
      { code: 'M', name: '中码', sortOrder: 2 },
      { params: { sizeGroupId: '10001' } },
    );
    expect(mockedPost).toHaveBeenNthCalledWith(
      3,
      '/master/size-group/size/update',
      { name: '中码更新' },
      { params: { sizeId: '10101' } },
    );
    expect(mockedPost).toHaveBeenNthCalledWith(4, '/master/size-group/size/delete', null, {
      params: { sizeId: '10101' },
    });
  });
});
