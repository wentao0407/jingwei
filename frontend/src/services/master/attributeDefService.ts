import { apiClient, unwrapApiResponse } from '@/services/http/apiClient';
import type { PageResult } from '@/services/master/customerService';
import { normalizeOptionalFields } from '@/services/shared/normalize';

export interface AttributeDefinitionRecord {
  id: string;
  code?: string | null;
  name?: string | null;
  materialType?: string | null;
  inputType?: string | null;
  required?: boolean | null;
  sortOrder?: number | null;
  options?: string[] | null;
  jsonbPath?: string | null;
  remark?: string | null;
  createdAt?: string | null;
  updatedAt?: string | null;
}

export interface SaveAttributeDefinitionPayload {
  code: string;
  name: string;
  materialType: string;
  inputType: string;
  required: boolean;
  sortOrder?: number;
  options?: string[];
  jsonbPath?: string;
  remark?: string;
}

export interface AttributeDefinitionQueryParams {
  current: number;
  size: number;
  materialType?: string;
  keyword?: string;
}

export async function pageAttributeDefinitions(
  params: AttributeDefinitionQueryParams,
): Promise<PageResult<AttributeDefinitionRecord>> {
  const response = await apiClient.post('/master/attr-def/page', normalizeOptionalFields({
    ...params,
    current: Math.max(1, params.current),
    size: Math.max(1, params.size),
  }));
  return unwrapApiResponse<PageResult<AttributeDefinitionRecord>>(response.data);
}

export async function getAttributeDefinitionDetail(id: string): Promise<AttributeDefinitionRecord> {
  const response = await apiClient.post('/master/attr-def/detail', null, { params: { id } });
  return unwrapApiResponse<AttributeDefinitionRecord>(response.data);
}

export async function createAttributeDefinition(
  payload: SaveAttributeDefinitionPayload,
): Promise<AttributeDefinitionRecord> {
  const response = await apiClient.post('/master/attr-def/create', normalizeOptionalFields(payload));
  return unwrapApiResponse<AttributeDefinitionRecord>(response.data);
}

export async function updateAttributeDefinition(
  id: string,
  payload: SaveAttributeDefinitionPayload,
): Promise<AttributeDefinitionRecord> {
  const response = await apiClient.post('/master/attr-def/update', normalizeOptionalFields(payload), {
    params: { id: id.trim() },
  });
  return unwrapApiResponse<AttributeDefinitionRecord>(response.data);
}

export async function deleteAttributeDefinition(id: string): Promise<void> {
  const response = await apiClient.post('/master/attr-def/delete', null, { params: { id } });
  return unwrapApiResponse<void>(response.data);
}

export async function listAttributeDefinitionsByType(
  materialType: string,
): Promise<AttributeDefinitionRecord[]> {
  const response = await apiClient.post('/master/attr-def/list-by-type', null, {
    params: { materialType },
  });
  return unwrapApiResponse<AttributeDefinitionRecord[]>(response.data);
}
