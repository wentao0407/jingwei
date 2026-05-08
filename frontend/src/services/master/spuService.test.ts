import { beforeEach, describe, expect, it, vi } from 'vitest';
import {
  addSpuColors,
  batchUpdateSkuPrice,
  createSpu,
  deactivateSku,
  deleteSpu,
  getSpuDetail,
  listSpus,
  updateSkuPrice,
  updateSpu,
} from './spuService';
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

describe('spuService', () => {
  beforeEach(() => {
    mockedPost.mockReset();
  });

  it('lists and gets SPU detail with normalized params', async () => {
    mockedPost
      .mockResolvedValueOnce({ data: { code: 0, message: 'success', success: true, data: [] } })
      .mockResolvedValueOnce({ data: { code: 0, message: 'success', success: true, data: { id: '1' } } });

    await listSpus({ status: ' ACTIVE ', categoryId: '', seasonId: '11' });
    await getSpuDetail('1');

    expect(mockedPost).toHaveBeenNthCalledWith(1, '/master/spu/list', null, {
      params: { status: 'ACTIVE', seasonId: '11' },
    });
    expect(mockedPost).toHaveBeenNthCalledWith(2, '/master/spu/detail', null, {
      params: { spuId: '1' },
    });
  });

  it('creates and updates SPUs with trimmed color payloads', async () => {
    mockedPost
      .mockResolvedValueOnce({ data: { code: 0, message: 'success', success: true, data: { id: '1' } } })
      .mockResolvedValueOnce({ data: { code: 0, message: 'success', success: true, data: { id: '1' } } });

    await createSpu({
      name: ' 春款衬衫 ',
      sizeGroupId: '10001',
      colors: [{ colorName: ' 黑色 ', colorCode: ' BK ', pantoneCode: '' }],
    });
    await updateSpu('1', { name: ' 春款衬衫更新 ', status: 'ACTIVE', remark: '' });

    expect(mockedPost).toHaveBeenNthCalledWith(1, '/master/spu/create', {
      name: '春款衬衫',
      sizeGroupId: '10001',
      colors: [{ colorName: '黑色', colorCode: 'BK' }],
    });
    expect(mockedPost).toHaveBeenNthCalledWith(
      2,
      '/master/spu/update',
      { name: '春款衬衫更新', status: 'ACTIVE' },
      { params: { spuId: '1' } },
    );
  });

  it('manages SPU colors, SKU prices and deactivate operations', async () => {
    mockedPost
      .mockResolvedValueOnce({ data: { code: 0, message: 'success', success: true, data: { id: '1' } } })
      .mockResolvedValueOnce({ data: { code: 0, message: 'success', success: true, data: { id: 'sku1' } } })
      .mockResolvedValueOnce({ data: { code: 0, message: 'success', success: true, data: 3 } })
      .mockResolvedValueOnce({ data: { code: 0, message: 'success', success: true, data: null } })
      .mockResolvedValueOnce({ data: { code: 0, message: 'success', success: true, data: null } });

    await addSpuColors('1', [{ colorName: ' 白色 ', colorCode: ' WT ' }]);
    await updateSkuPrice({ skuId: 'sku1', salePrice: 199 });
    await batchUpdateSkuPrice({ spuId: '1', wholesalePrice: 88 });
    await deactivateSku('sku1');
    await deleteSpu('1');

    expect(mockedPost).toHaveBeenNthCalledWith(
      1,
      '/master/spu/addColor',
      { colors: [{ colorName: '白色', colorCode: 'WT' }] },
      { params: { spuId: '1' } },
    );
    expect(mockedPost).toHaveBeenNthCalledWith(2, '/master/sku/updatePrice', {
      skuId: 'sku1',
      salePrice: 199,
    });
    expect(mockedPost).toHaveBeenNthCalledWith(3, '/master/sku/batchUpdatePrice', {
      spuId: '1',
      wholesalePrice: 88,
    });
    expect(mockedPost).toHaveBeenNthCalledWith(4, '/master/sku/deactivate', null, {
      params: { skuId: 'sku1' },
    });
    expect(mockedPost).toHaveBeenNthCalledWith(5, '/master/spu/delete', null, {
      params: { spuId: '1' },
    });
  });
});
