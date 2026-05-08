import { apiClient, unwrapApiResponse } from '@/services/http/apiClient';
import type { PageResult } from '@/services/master/customerService';

export interface MaterialQueryParams {
  current: number;
  size: number;
  type?: string;
  categoryId?: string;
  status?: string;
  keyword?: string;
}

export interface MaterialRecord {
  id: string;
  code: string;
  name: string;
  type: string;
  categoryId: string;
  unit: string;
  status: string;
  extAttrs?: Record<string, unknown> | null;
  remark?: string | null;
  createdAt?: string | null;
  updatedAt?: string | null;
}

export interface AttributeDefRecord {
  id: string;
  code: string;
  name: string;
  materialType: string;
  inputType: string;
  required?: boolean | null;
  sortOrder?: number | null;
  options?: string[] | null;
  extJsonPath?: string | null;
}

export interface CreateMaterialPayload {
  name: string;
  type: string;
  categoryId: string;
  unit: string;
  extAttrs?: Record<string, unknown>;
  remark?: string;
}

export type UpdateMaterialPayload = Omit<CreateMaterialPayload, 'type'> & {
  status?: string;
};

export async function listMaterials(params: MaterialQueryParams): Promise<PageResult<MaterialRecord>> {
  const response = await apiClient.post('/master/material/page', normalizeQuery(params));
  return unwrapApiResponse<PageResult<MaterialRecord>>(response.data);
}

export async function createMaterial(payload: CreateMaterialPayload): Promise<MaterialRecord> {
  const response = await apiClient.post('/master/material/create', normalizeOptionalFields(payload));
  return unwrapApiResponse<MaterialRecord>(response.data);
}

export async function updateMaterial(materialId: string, payload: UpdateMaterialPayload): Promise<MaterialRecord> {
  const response = await apiClient.post('/master/material/update', normalizeOptionalFields(payload), {
    params: { materialId },
  });
  return unwrapApiResponse<MaterialRecord>(response.data);
}

export async function deactivateMaterial(materialId: string): Promise<void> {
  const response = await apiClient.post('/master/material/deactivate', null, { params: { materialId } });
  return unwrapApiResponse<void>(response.data);
}

export async function getMaterialAttributeDefs(materialType: string): Promise<AttributeDefRecord[]> {
  const response = await apiClient.post('/master/material/attributeDefs', null, {
    params: { materialType },
  });
  return unwrapApiResponse<AttributeDefRecord[]>(response.data);
}

function normalizeQuery<T extends MaterialQueryParams>(params: T): T {
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
