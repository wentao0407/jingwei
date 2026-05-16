import { apiClient, unwrapApiResponse } from '@/services/http/apiClient';
import type { PageResult } from '@/services/master/customerService';
import { normalizeOptionalFields } from '@/services/shared/normalize';

export interface MaterialIssueLineRecord {
  id?: string;
  materialId?: string | null;
  materialCode?: string | null;
  materialName?: string | null;
  batchNo?: string | null;
  quantity?: number | null;
  unit?: string | null;
  remark?: string | null;
}

export interface MaterialIssueRecord {
  id: string;
  issueNo?: string | null;
  productionOrderId?: string | null;
  productionLineId?: string | null;
  status?: string | null;
  statusLabel?: string | null;
  remark?: string | null;
  createdAt?: string | null;
  lines?: MaterialIssueLineRecord[] | null;
}

export interface CreateMaterialIssueLinePayload {
  materialId: string;
  batchNo?: string;
  quantity: number;
  unit?: string;
  remark?: string;
}

export interface CreateMaterialIssuePayload {
  productionOrderId: string;
  warehouseId: string;
  productionLineId?: string;
  remark?: string;
  lines: CreateMaterialIssueLinePayload[];
}

export interface MaterialIssueQueryParams {
  current: number;
  size: number;
  status?: string;
}

export async function pageMaterialIssues(params: MaterialIssueQueryParams): Promise<PageResult<MaterialIssueRecord>> {
  const response = await apiClient.post('/warehouse/material-issue/page', normalizeOptionalFields({
    ...params,
    current: Math.max(1, params.current),
    size: Math.max(1, params.size),
  }));
  return unwrapApiResponse<PageResult<MaterialIssueRecord>>(response.data);
}

export async function getMaterialIssueDetail(issueId: string): Promise<MaterialIssueRecord> {
  const response = await apiClient.post('/warehouse/material-issue/detail', null, { params: { issueId } });
  return unwrapApiResponse<MaterialIssueRecord>(response.data);
}

export async function createMaterialIssue(payload: CreateMaterialIssuePayload): Promise<MaterialIssueRecord> {
  const response = await apiClient.post('/warehouse/material-issue/create', normalizeOptionalFields({
    ...payload,
    lines: payload.lines.map((line) => normalizeOptionalFields(line)),
  }));
  return unwrapApiResponse<MaterialIssueRecord>(response.data);
}

export async function confirmMaterialIssue(issueId: string): Promise<void> {
  const response = await apiClient.post('/warehouse/material-issue/confirm', null, { params: { issueId } });
  return unwrapApiResponse<void>(response.data);
}
