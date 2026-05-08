import { apiClient, unwrapApiResponse } from '@/services/http/apiClient';

export interface SizeGroupQueryParams {
  category?: string;
  status?: string;
}

export interface SizeRecord {
  id: string;
  sizeGroupId: string;
  code: string;
  name: string;
  sortOrder?: number | null;
  createdAt?: string | null;
  updatedAt?: string | null;
}

export interface SizeGroupRecord {
  id: string;
  code: string;
  name: string;
  category: string;
  status: string;
  sizes?: SizeRecord[] | null;
  createdAt?: string | null;
  updatedAt?: string | null;
}

export interface CreateSizeGroupPayload {
  code: string;
  name: string;
  category: string;
}

export interface UpdateSizeGroupPayload {
  name?: string;
  category?: string;
  status?: string;
}

export interface CreateSizePayload {
  code: string;
  name: string;
  sortOrder?: number;
}

export interface UpdateSizePayload {
  code?: string;
  name?: string;
  sortOrder?: number;
}

export async function listSizeGroups(params: SizeGroupQueryParams = {}): Promise<SizeGroupRecord[]> {
  const response = await apiClient.post('/master/size-group/list', null, {
    params: normalizeOptionalFields(params),
  });
  return unwrapApiResponse<SizeGroupRecord[]>(response.data);
}

export async function getSizeGroupDetail(sizeGroupId: string): Promise<SizeGroupRecord> {
  const response = await apiClient.post('/master/size-group/detail', null, {
    params: { sizeGroupId },
  });
  return unwrapApiResponse<SizeGroupRecord>(response.data);
}

export async function createSizeGroup(payload: CreateSizeGroupPayload): Promise<SizeGroupRecord> {
  const response = await apiClient.post('/master/size-group/create', normalizeOptionalFields(payload));
  return unwrapApiResponse<SizeGroupRecord>(response.data);
}

export async function updateSizeGroup(
  sizeGroupId: string,
  payload: UpdateSizeGroupPayload,
): Promise<SizeGroupRecord> {
  const response = await apiClient.post('/master/size-group/update', normalizeOptionalFields(payload), {
    params: { sizeGroupId },
  });
  return unwrapApiResponse<SizeGroupRecord>(response.data);
}

export async function deleteSizeGroup(sizeGroupId: string): Promise<void> {
  const response = await apiClient.post('/master/size-group/delete', null, {
    params: { sizeGroupId },
  });
  return unwrapApiResponse<void>(response.data);
}

export async function createSize(sizeGroupId: string, payload: CreateSizePayload): Promise<SizeRecord> {
  const response = await apiClient.post('/master/size-group/size/create', normalizeOptionalFields(payload), {
    params: { sizeGroupId },
  });
  return unwrapApiResponse<SizeRecord>(response.data);
}

export async function updateSize(sizeId: string, payload: UpdateSizePayload): Promise<SizeRecord> {
  const response = await apiClient.post('/master/size-group/size/update', normalizeOptionalFields(payload), {
    params: { sizeId },
  });
  return unwrapApiResponse<SizeRecord>(response.data);
}

export async function deleteSize(sizeId: string): Promise<void> {
  const response = await apiClient.post('/master/size-group/size/delete', null, {
    params: { sizeId },
  });
  return unwrapApiResponse<void>(response.data);
}

function normalizeOptionalFields<T extends object>(payload: T): Partial<T> {
  return Object.fromEntries(
    Object.entries(payload)
      .map(([key, value]) => [key, typeof value === 'string' ? value.trim() : value])
      .filter(([, value]) => value !== undefined && value !== null && value !== ''),
  ) as Partial<T>;
}
