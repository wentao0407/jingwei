import { beforeEach, describe, expect, it, vi } from 'vitest';
import {
  activateWarehouse,
  createLocation,
  createWarehouse,
  deactivateLocation,
  deactivateWarehouse,
  deleteLocation,
  deleteWarehouse,
  freezeLocation,
  getWarehouseDetail,
  listWarehouses,
  pageWarehouses,
  unfreezeLocation,
  updateLocation,
  updateWarehouse,
} from './warehouseService';
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

describe('warehouseService', () => {
  beforeEach(() => {
    mockedPost.mockReset();
  });

  it('queries warehouses with normalized params', async () => {
    mockedPost
      .mockResolvedValueOnce({ data: { code: 0, message: 'success', success: true, data: { records: [] } } })
      .mockResolvedValueOnce({ data: { code: 0, message: 'success', success: true, data: [] } })
      .mockResolvedValueOnce({ data: { code: 0, message: 'success', success: true, data: { id: '10001' } } });

    await pageWarehouses({ current: 0, size: 0, keyword: ' WH ', type: '', status: 'ACTIVE' });
    await listWarehouses({ type: ' RAW_MATERIAL ', status: '' });
    await getWarehouseDetail('10001');

    expect(mockedPost).toHaveBeenNthCalledWith(1, '/master/warehouse/page', {
      current: 1,
      size: 1,
      keyword: 'WH',
      status: 'ACTIVE',
    });
    expect(mockedPost).toHaveBeenNthCalledWith(2, '/master/warehouse/list', null, {
      params: { type: 'RAW_MATERIAL' },
    });
    expect(mockedPost).toHaveBeenNthCalledWith(3, '/master/warehouse/detail', null, {
      params: { warehouseId: '10001' },
    });
  });

  it('creates, updates, activates, deactivates and deletes warehouses', async () => {
    mockedPost
      .mockResolvedValueOnce({ data: { code: 0, message: 'success', success: true, data: { id: '10001' } } })
      .mockResolvedValueOnce({ data: { code: 0, message: 'success', success: true, data: { id: '10001' } } })
      .mockResolvedValueOnce({ data: { code: 0, message: 'success', success: true, data: null } })
      .mockResolvedValueOnce({ data: { code: 0, message: 'success', success: true, data: null } })
      .mockResolvedValueOnce({ data: { code: 0, message: 'success', success: true, data: null } });

    await createWarehouse({ code: ' WH01 ', name: ' 主仓 ', type: 'FINISHED_GOODS', address: '', remark: ' 备注 ' });
    await updateWarehouse('10001', { name: ' 主仓更新 ', remark: '' });
    await activateWarehouse('10001');
    await deactivateWarehouse('10001');
    await deleteWarehouse('10001');

    expect(mockedPost).toHaveBeenNthCalledWith(1, '/master/warehouse/create', {
      code: 'WH01',
      name: '主仓',
      type: 'FINISHED_GOODS',
      remark: '备注',
    });
    expect(mockedPost).toHaveBeenNthCalledWith(
      2,
      '/master/warehouse/update',
      { name: '主仓更新' },
      { params: { warehouseId: '10001' } },
    );
    expect(mockedPost).toHaveBeenNthCalledWith(3, '/master/warehouse/activate', null, {
      params: { warehouseId: '10001' },
    });
    expect(mockedPost).toHaveBeenNthCalledWith(4, '/master/warehouse/deactivate', null, {
      params: { warehouseId: '10001' },
    });
    expect(mockedPost).toHaveBeenNthCalledWith(5, '/master/warehouse/delete', null, {
      params: { warehouseId: '10001' },
    });
  });

  it('manages locations under warehouses', async () => {
    mockedPost
      .mockResolvedValueOnce({ data: { code: 0, message: 'success', success: true, data: { id: '20001' } } })
      .mockResolvedValueOnce({ data: { code: 0, message: 'success', success: true, data: { id: '20001' } } })
      .mockResolvedValueOnce({ data: { code: 0, message: 'success', success: true, data: null } })
      .mockResolvedValueOnce({ data: { code: 0, message: 'success', success: true, data: null } })
      .mockResolvedValueOnce({ data: { code: 0, message: 'success', success: true, data: null } })
      .mockResolvedValueOnce({ data: { code: 0, message: 'success', success: true, data: null } });

    await createLocation('10001', {
      zoneCode: ' A ',
      rackCode: ' 01 ',
      rowCode: ' 02 ',
      binCode: ' 03 ',
      locationType: 'STORAGE',
      capacity: 100,
      remark: '',
    });
    await updateLocation('20001', { capacity: 120, remark: ' 新备注 ' });
    await freezeLocation('20001');
    await unfreezeLocation('20001');
    await deactivateLocation('20001');
    await deleteLocation('20001');

    expect(mockedPost).toHaveBeenNthCalledWith(
      1,
      '/master/warehouse/location/create',
      { zoneCode: 'A', rackCode: '01', rowCode: '02', binCode: '03', locationType: 'STORAGE', capacity: 100 },
      { params: { warehouseId: '10001' } },
    );
    expect(mockedPost).toHaveBeenNthCalledWith(
      2,
      '/master/warehouse/location/update',
      { capacity: 120, remark: '新备注' },
      { params: { locationId: '20001' } },
    );
    expect(mockedPost).toHaveBeenNthCalledWith(3, '/master/warehouse/location/freeze', null, {
      params: { locationId: '20001' },
    });
    expect(mockedPost).toHaveBeenNthCalledWith(4, '/master/warehouse/location/unfreeze', null, {
      params: { locationId: '20001' },
    });
    expect(mockedPost).toHaveBeenNthCalledWith(5, '/master/warehouse/location/deactivate', null, {
      params: { locationId: '20001' },
    });
    expect(mockedPost).toHaveBeenNthCalledWith(6, '/master/warehouse/location/delete', null, {
      params: { locationId: '20001' },
    });
  });
});
