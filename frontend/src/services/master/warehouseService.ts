import { apiClient, unwrapApiResponse } from '@/services/http/apiClient';
import type { PageResult } from '@/services/master/customerService';

export interface WarehouseQueryParams {
  current: number;
  size: number;
  keyword?: string;
  type?: string;
  status?: string;
}

export interface WarehouseListParams {
  type?: string;
  status?: string;
}

export interface LocationRecord {
  id: string;
  warehouseId: string;
  zoneCode: string;
  rackCode: string;
  rowCode: string;
  binCode: string;
  fullCode: string;
  locationType: string;
  capacity?: number | null;
  usedCapacity?: number | null;
  status: string;
  remark?: string | null;
  createdAt?: string | null;
  updatedAt?: string | null;
}

export interface WarehouseRecord {
  id: string;
  code: string;
  name: string;
  type: string;
  address?: string | null;
  managerId?: string | null;
  status: string;
  remark?: string | null;
  locations?: LocationRecord[] | null;
  createdAt?: string | null;
  updatedAt?: string | null;
}

export interface CreateWarehousePayload {
  code: string;
  name: string;
  type: string;
  address?: string;
  managerId?: string;
  remark?: string;
}

export interface UpdateWarehousePayload {
  name?: string;
  address?: string;
  managerId?: string;
  remark?: string;
}

export interface CreateLocationPayload {
  zoneCode: string;
  rackCode: string;
  rowCode: string;
  binCode: string;
  locationType: string;
  capacity?: number;
  remark?: string;
}

export interface UpdateLocationPayload {
  capacity?: number;
  remark?: string;
}

export async function pageWarehouses(params: WarehouseQueryParams): Promise<PageResult<WarehouseRecord>> {
  const response = await apiClient.post('/master/warehouse/page', normalizeWarehouseQuery(params));
  return unwrapApiResponse<PageResult<WarehouseRecord>>(response.data);
}

export async function listWarehouses(params: WarehouseListParams = {}): Promise<WarehouseRecord[]> {
  const response = await apiClient.post('/master/warehouse/list', null, {
    params: normalizeOptionalFields(params),
  });
  return unwrapApiResponse<WarehouseRecord[]>(response.data);
}

export async function getWarehouseDetail(warehouseId: string): Promise<WarehouseRecord> {
  const response = await apiClient.post('/master/warehouse/detail', null, {
    params: { warehouseId },
  });
  return unwrapApiResponse<WarehouseRecord>(response.data);
}

export async function createWarehouse(payload: CreateWarehousePayload): Promise<WarehouseRecord> {
  const response = await apiClient.post('/master/warehouse/create', normalizeOptionalFields(payload));
  return unwrapApiResponse<WarehouseRecord>(response.data);
}

export async function updateWarehouse(
  warehouseId: string,
  payload: UpdateWarehousePayload,
): Promise<WarehouseRecord> {
  const response = await apiClient.post('/master/warehouse/update', normalizeOptionalFields(payload), {
    params: { warehouseId },
  });
  return unwrapApiResponse<WarehouseRecord>(response.data);
}

export async function activateWarehouse(warehouseId: string): Promise<void> {
  const response = await apiClient.post('/master/warehouse/activate', null, { params: { warehouseId } });
  return unwrapApiResponse<void>(response.data);
}

export async function deactivateWarehouse(warehouseId: string): Promise<void> {
  const response = await apiClient.post('/master/warehouse/deactivate', null, { params: { warehouseId } });
  return unwrapApiResponse<void>(response.data);
}

export async function deleteWarehouse(warehouseId: string): Promise<void> {
  const response = await apiClient.post('/master/warehouse/delete', null, { params: { warehouseId } });
  return unwrapApiResponse<void>(response.data);
}

export async function createLocation(warehouseId: string, payload: CreateLocationPayload): Promise<LocationRecord> {
  const response = await apiClient.post('/master/warehouse/location/create', normalizeOptionalFields(payload), {
    params: { warehouseId },
  });
  return unwrapApiResponse<LocationRecord>(response.data);
}

export async function updateLocation(locationId: string, payload: UpdateLocationPayload): Promise<LocationRecord> {
  const response = await apiClient.post('/master/warehouse/location/update', normalizeOptionalFields(payload), {
    params: { locationId },
  });
  return unwrapApiResponse<LocationRecord>(response.data);
}

export async function freezeLocation(locationId: string): Promise<void> {
  const response = await apiClient.post('/master/warehouse/location/freeze', null, { params: { locationId } });
  return unwrapApiResponse<void>(response.data);
}

export async function unfreezeLocation(locationId: string): Promise<void> {
  const response = await apiClient.post('/master/warehouse/location/unfreeze', null, { params: { locationId } });
  return unwrapApiResponse<void>(response.data);
}

export async function deactivateLocation(locationId: string): Promise<void> {
  const response = await apiClient.post('/master/warehouse/location/deactivate', null, { params: { locationId } });
  return unwrapApiResponse<void>(response.data);
}

export async function deleteLocation(locationId: string): Promise<void> {
  const response = await apiClient.post('/master/warehouse/location/delete', null, { params: { locationId } });
  return unwrapApiResponse<void>(response.data);
}

function normalizeWarehouseQuery(params: WarehouseQueryParams): WarehouseQueryParams {
  return normalizeOptionalFields({
    ...params,
    current: Math.max(1, params.current),
    size: Math.max(1, params.size),
  }) as WarehouseQueryParams;
}

function normalizeOptionalFields<T extends object>(payload: T): Partial<T> {
  return Object.fromEntries(
    Object.entries(payload)
      .map(([key, value]) => [key, typeof value === 'string' ? value.trim() : value])
      .filter(([, value]) => value !== undefined && value !== null && value !== ''),
  ) as Partial<T>;
}
