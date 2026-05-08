import { apiClient, unwrapApiResponse } from '@/services/http/apiClient';
import type { PageResult } from './customerService';

export interface SupplierQueryParams {
  current: number;
  size: number;
  keyword?: string;
  type?: string;
  qualificationStatus?: string;
  status?: string;
}

export interface SupplierRecord {
  id: string;
  code: string;
  name: string;
  shortName?: string | null;
  type: string;
  contactPerson?: string | null;
  contactPhone?: string | null;
  address?: string | null;
  settlementType?: string | null;
  leadTimeDays?: number | null;
  qualificationStatus?: string | null;
  status: string;
  remark?: string | null;
  createdAt?: string | null;
  updatedAt?: string | null;
}

export interface CreateSupplierPayload {
  name: string;
  shortName?: string;
  type: string;
  contactPerson?: string;
  contactPhone?: string;
  address?: string;
  settlementType?: string;
  leadTimeDays?: number;
  remark?: string;
}

export interface UpdateSupplierPayload extends Omit<CreateSupplierPayload, 'type'> {
  qualificationStatus?: string;
}

export async function listSuppliers(params: SupplierQueryParams): Promise<PageResult<SupplierRecord>> {
  const response = await apiClient.post('/master/supplier/page', normalizeQuery(params));
  return unwrapApiResponse<PageResult<SupplierRecord>>(response.data);
}

export async function createSupplier(payload: CreateSupplierPayload): Promise<SupplierRecord> {
  const response = await apiClient.post('/master/supplier/create', normalizeOptionalFields(payload));
  return unwrapApiResponse<SupplierRecord>(response.data);
}

export async function updateSupplier(supplierId: string, payload: UpdateSupplierPayload): Promise<SupplierRecord> {
  const response = await apiClient.post('/master/supplier/update', normalizeOptionalFields(payload), {
    params: { supplierId },
  });
  return unwrapApiResponse<SupplierRecord>(response.data);
}

export async function deactivateSupplier(supplierId: string): Promise<void> {
  const response = await apiClient.post('/master/supplier/deactivate', null, { params: { supplierId } });
  return unwrapApiResponse<void>(response.data);
}

export async function activateSupplier(supplierId: string): Promise<void> {
  const response = await apiClient.post('/master/supplier/activate', null, { params: { supplierId } });
  return unwrapApiResponse<void>(response.data);
}

export async function deleteSupplier(supplierId: string): Promise<void> {
  const response = await apiClient.post('/master/supplier/delete', null, { params: { supplierId } });
  return unwrapApiResponse<void>(response.data);
}

function normalizeQuery<T extends SupplierQueryParams>(params: T): T {
  return normalizeOptionalFields({
    ...params,
    current: Math.max(1, params.current),
    size: Math.max(1, params.size),
  }) as T;
}

function normalizeOptionalFields<T extends object>(payload: T): T {
  return Object.fromEntries(
    Object.entries(payload)
      .map(([key, value]) => [key, typeof value === 'string' ? value.trim() : value])
      .filter(([, value]) => value !== undefined && value !== null && value !== ''),
  ) as T;
}
