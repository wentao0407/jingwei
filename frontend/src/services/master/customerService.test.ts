import { beforeEach, describe, expect, it, vi } from 'vitest';
import { apiClient } from '@/services/http/apiClient';
import { activateCustomer, createCustomer, deactivateCustomer, deleteCustomer, listCustomers, updateCustomer } from './customerService';

describe('customerService', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

  it('loads paged customers with normalized query', async () => {
    const postSpy = vi.spyOn(apiClient, 'post').mockResolvedValue({
      data: {
        code: 0,
        message: 'success',
        success: true,
        data: { records: [], total: 0, current: 1, size: 10, pages: 0 },
      },
    });

    await listCustomers({ current: 0, size: 10, keyword: ' 云织 ', type: 'WHOLESALE', level: '', status: 'ACTIVE' });

    expect(postSpy).toHaveBeenCalledWith('/master/customer/page', {
      current: 1,
      size: 10,
      keyword: '云织',
      type: 'WHOLESALE',
      status: 'ACTIVE',
    });
  });

  it('creates customers with trimmed optional fields', async () => {
    const postSpy = vi.spyOn(apiClient, 'post').mockResolvedValue({
      data: { code: 0, message: 'success', success: true, data: { id: '1', code: 'CUS-000001' } },
    });

    await createCustomer({
      name: ' 杭州云织 ',
      shortName: '',
      type: 'WHOLESALE',
      level: 'A',
      contactPerson: ' 王经理 ',
      contactPhone: '13800138000',
      address: ' 杭州 ',
      deliveryAddress: '',
      settlementType: 'MONTHLY',
      creditLimit: 500000,
      remark: '',
    });

    expect(postSpy).toHaveBeenCalledWith('/master/customer/create', {
      name: '杭州云织',
      type: 'WHOLESALE',
      level: 'A',
      contactPerson: '王经理',
      contactPhone: '13800138000',
      address: '杭州',
      settlementType: 'MONTHLY',
      creditLimit: 500000,
    });
  });

  it('updates customer by customerId query param', async () => {
    const postSpy = vi.spyOn(apiClient, 'post').mockResolvedValue({
      data: { code: 0, message: 'success', success: true, data: { id: '1', code: 'CUS-000001' } },
    });

    await updateCustomer('2051932034979037191', { name: ' 上海锦棉 ', level: 'B' });

    expect(postSpy).toHaveBeenCalledWith(
      '/master/customer/update',
      { name: '上海锦棉', level: 'B' },
      { params: { customerId: '2051932034979037191' } },
    );
  });

  it('changes customer status and deletes by customerId query param', async () => {
    const postSpy = vi.spyOn(apiClient, 'post').mockResolvedValue({
      data: { code: 0, message: 'success', success: true, data: null },
    });

    await deactivateCustomer('1');
    await activateCustomer('2');
    await deleteCustomer('3');

    expect(postSpy).toHaveBeenCalledWith('/master/customer/deactivate', null, { params: { customerId: '1' } });
    expect(postSpy).toHaveBeenCalledWith('/master/customer/activate', null, { params: { customerId: '2' } });
    expect(postSpy).toHaveBeenCalledWith('/master/customer/delete', null, { params: { customerId: '3' } });
  });
});
