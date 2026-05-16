import { apiClient, unwrapApiResponse } from '@/services/http/apiClient';
import { normalizeOptionalFields } from '@/services/shared/normalize';

export interface PageResult<T> {
  records: T[];
  total: number;
  size: number;
  current: number;
  pages: number;
}

export interface CustomerQueryParams {
  current: number;
  size: number;
  keyword?: string;
  type?: string;
  level?: string;
  status?: string;
}

export interface CustomerRecord {
  id: string;
  code: string;
  name: string;
  shortName?: string | null;
  type: string;
  level?: string | null;
  contactPerson?: string | null;
  contactPhone?: string | null;
  address?: string | null;
  deliveryAddress?: string | null;
  settlementType?: string | null;
  creditLimit?: number | null;
  status: string;
  remark?: string | null;
  createdAt?: string | null;
  updatedAt?: string | null;
}

export interface CreateCustomerPayload {
  name: string;
  shortName?: string;
  type: string;
  level?: string;
  contactPerson?: string;
  contactPhone?: string;
  address?: string;
  deliveryAddress?: string;
  settlementType?: string;
  creditLimit?: number;
  remark?: string;
}

export type UpdateCustomerPayload = Omit<CreateCustomerPayload, 'type'>;

export async function listCustomers(params: CustomerQueryParams): Promise<PageResult<CustomerRecord>> {
  const response = await apiClient.post('/master/customer/page', normalizeQuery(params));
  return unwrapApiResponse<PageResult<CustomerRecord>>(response.data);
}

export async function createCustomer(payload: CreateCustomerPayload): Promise<CustomerRecord> {
  const response = await apiClient.post('/master/customer/create', normalizeOptionalFields(payload));
  return unwrapApiResponse<CustomerRecord>(response.data);
}

export async function updateCustomer(customerId: string, payload: UpdateCustomerPayload): Promise<CustomerRecord> {
  const response = await apiClient.post('/master/customer/update', normalizeOptionalFields(payload), {
    params: { customerId },
  });
  return unwrapApiResponse<CustomerRecord>(response.data);
}

export async function deactivateCustomer(customerId: string): Promise<void> {
  const response = await apiClient.post('/master/customer/deactivate', null, { params: { customerId } });
  return unwrapApiResponse<void>(response.data);
}

export async function activateCustomer(customerId: string): Promise<void> {
  const response = await apiClient.post('/master/customer/activate', null, { params: { customerId } });
  return unwrapApiResponse<void>(response.data);
}

export async function deleteCustomer(customerId: string): Promise<void> {
  const response = await apiClient.post('/master/customer/delete', null, { params: { customerId } });
  return unwrapApiResponse<void>(response.data);
}

function normalizeQuery<T extends CustomerQueryParams>(params: T): T {
  return normalizeOptionalFields({
    ...params,
    current: Math.max(1, params.current),
    size: Math.max(1, params.size),
  }) as T;
}

