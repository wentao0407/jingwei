import { apiClient, unwrapApiResponse } from '@/services/http/apiClient';
import type { PageResult } from '@/services/master/customerService';
import { normalizeOptionalFields } from '@/services/shared/normalize';

export interface MaterialReturnLineRecord {
  id?: string;
  materialId?: string | null;
  materialCode?: string | null;
  materialName?: string | null;
  batchNo?: string | null;
  quantity?: number | null;
  unit?: string | null;
  remark?: string | null;
}

export interface MaterialReturnRecord {
  id: string;
  returnNo?: string | null;
  productionOrderId?: string | null;
  status?: string | null;
  statusLabel?: string | null;
  remark?: string | null;
  createdAt?: string | null;
  lines?: MaterialReturnLineRecord[] | null;
}

export interface CreateMaterialReturnLinePayload {
  materialId: string;
  batchNo?: string;
  quantity: number;
  unit?: string;
  remark?: string;
}

export interface CreateMaterialReturnPayload {
  productionOrderId: string;
  remark?: string;
  lines: CreateMaterialReturnLinePayload[];
}

export interface MaterialReturnQueryParams {
  current: number;
  size: number;
  status?: string;
}

export async function pageMaterialReturns(params: MaterialReturnQueryParams): Promise<PageResult<MaterialReturnRecord>> {
  const response = await apiClient.post('/warehouse/material-return/page', normalizeOptionalFields({
    ...params,
    current: Math.max(1, params.current),
    size: Math.max(1, params.size),
  }));
  return unwrapApiResponse<PageResult<MaterialReturnRecord>>(response.data);
}

export async function getMaterialReturnDetail(returnId: string): Promise<MaterialReturnRecord> {
  const response = await apiClient.post('/warehouse/material-return/detail', null, { params: { returnId } });
  return unwrapApiResponse<MaterialReturnRecord>(response.data);
}

export async function createMaterialReturn(payload: CreateMaterialReturnPayload): Promise<MaterialReturnRecord> {
  const response = await apiClient.post('/warehouse/material-return/create', normalizeOptionalFields({
    ...payload,
    lines: payload.lines.map((line) => normalizeOptionalFields(line)),
  }));
  return unwrapApiResponse<MaterialReturnRecord>(response.data);
}

export async function confirmMaterialReturn(returnId: string): Promise<void> {
  const response = await apiClient.post('/warehouse/material-return/confirm', null, { params: { returnId } });
  return unwrapApiResponse<void>(response.data);
}
