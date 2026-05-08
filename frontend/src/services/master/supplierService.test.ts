import { beforeEach, describe, expect, it, vi } from 'vitest';
import { apiClient } from '@/services/http/apiClient';
import { activateSupplier, createSupplier, deactivateSupplier, deleteSupplier, listSuppliers, updateSupplier } from './supplierService';

describe('supplierService', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

  it('loads paged suppliers with normalized query', async () => {
    const postSpy = vi.spyOn(apiClient, 'post').mockResolvedValue({
      data: {
        code: 0,
        message: 'success',
        success: true,
        data: { records: [], total: 0, current: 1, size: 10, pages: 0 },
      },
    });

    await listSuppliers({ current: 1, size: 10, keyword: ' 面料 ', type: 'FABRIC', qualificationStatus: '', status: 'ACTIVE' });

    expect(postSpy).toHaveBeenCalledWith('/master/supplier/page', {
      current: 1,
      size: 10,
      keyword: '面料',
      type: 'FABRIC',
      status: 'ACTIVE',
    });
  });

  it('creates suppliers with trimmed optional fields', async () => {
    const postSpy = vi.spyOn(apiClient, 'post').mockResolvedValue({
      data: { code: 0, message: 'success', success: true, data: { id: '1', code: 'SUP-000001' } },
    });

    await createSupplier({
      name: ' 绍兴面料厂 ',
      shortName: '',
      type: 'FABRIC',
      contactPerson: ' 李经理 ',
      contactPhone: '13900139000',
      address: ' 绍兴 ',
      settlementType: 'MONTHLY',
      leadTimeDays: 7,
      remark: '',
    });

    expect(postSpy).toHaveBeenCalledWith('/master/supplier/create', {
      name: '绍兴面料厂',
      type: 'FABRIC',
      contactPerson: '李经理',
      contactPhone: '13900139000',
      address: '绍兴',
      settlementType: 'MONTHLY',
      leadTimeDays: 7,
    });
  });

  it('updates supplier by supplierId query param', async () => {
    const postSpy = vi.spyOn(apiClient, 'post').mockResolvedValue({
      data: { code: 0, message: 'success', success: true, data: { id: '1', code: 'SUP-000001' } },
    });

    await updateSupplier('2051932034979037191', { name: ' 宁波辅料厂 ', qualificationStatus: 'QUALIFIED' });

    expect(postSpy).toHaveBeenCalledWith(
      '/master/supplier/update',
      { name: '宁波辅料厂', qualificationStatus: 'QUALIFIED' },
      { params: { supplierId: '2051932034979037191' } },
    );
  });

  it('changes supplier status and deletes by supplierId query param', async () => {
    const postSpy = vi.spyOn(apiClient, 'post').mockResolvedValue({
      data: { code: 0, message: 'success', success: true, data: null },
    });

    await deactivateSupplier('1');
    await activateSupplier('2');
    await deleteSupplier('3');

    expect(postSpy).toHaveBeenCalledWith('/master/supplier/deactivate', null, { params: { supplierId: '1' } });
    expect(postSpy).toHaveBeenCalledWith('/master/supplier/activate', null, { params: { supplierId: '2' } });
    expect(postSpy).toHaveBeenCalledWith('/master/supplier/delete', null, { params: { supplierId: '3' } });
  });
});
