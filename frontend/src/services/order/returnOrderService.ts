import { apiClient, unwrapApiResponse } from '@/services/http/apiClient';
import type { PageResult } from '@/services/master/customerService';
import { normalizeOptionalFields } from '@/services/shared/normalize';

export interface ReturnOrderLinePayload {
  salesOrderLineId: string;
  spuId: string;
  colorWayId: string;
  sizeMatrixJson: string;
  totalQuantity: number;
  remark?: string;
}

export interface CreateReturnOrderPayload {
  returnType: string;
  salesOrderId: string;
  salesOrderNo: string;
  customerId: string;
  reason?: string;
  remark?: string;
  lines: ReturnOrderLinePayload[];
}

export interface ReturnOrderLineRecord {
  id: string;
  returnId?: string | null;
  salesOrderLineId?: string | null;
  spuId?: string | null;
  colorWayId?: string | null;
  sizeMatrixJson?: string | null;
  totalQuantity?: number | null;
  qcPassedQty?: number | null;
  qcFailedQty?: number | null;
  qcResult?: string | null;
  remark?: string | null;
}

export interface ReturnOrderRecord {
  id: string;
  returnNo: string;
  returnType?: string | null;
  returnTypeLabel?: string | null;
  salesOrderId?: string | null;
  salesOrderNo?: string | null;
  customerId?: string | null;
  reason?: string | null;
  status?: string | null;
  statusLabel?: string | null;
  totalQuantity?: number | null;
  inboundOrderId?: string | null;
  approvedBy?: string | null;
  approvedAt?: string | null;
  remark?: string | null;
  createdAt?: string | null;
  lines?: ReturnOrderLineRecord[] | null;
}

export interface ReturnQcResultPayload {
  lineId: string;
  passedQty: number;
  failedQty: number;
  qcResult?: string;
  remark?: string;
}

export interface ReturnQcPayload {
  returnId: string;
  results: ReturnQcResultPayload[];
}

export interface ReturnOrderQueryParams {
  current: number;
  size: number;
  customerId?: string;
  status?: string;
}

export async function pageReturnOrders(params: ReturnOrderQueryParams): Promise<PageResult<ReturnOrderRecord>> {
  const response = await apiClient.post('/order/return/page', normalizePageQuery(params));
  return unwrapApiResponse<PageResult<ReturnOrderRecord>>(response.data);
}

export async function createReturnOrder(payload: CreateReturnOrderPayload): Promise<ReturnOrderRecord> {
  const response = await apiClient.post('/order/return/create', normalizeReturnOrderPayload(payload));
  return unwrapApiResponse<ReturnOrderRecord>(response.data);
}

export async function getReturnOrderDetail(returnId: string): Promise<ReturnOrderRecord> {
  const response = await postReturnId('/order/return/detail', returnId);
  return unwrapApiResponse<ReturnOrderRecord>(response.data);
}

export async function submitReturnOrder(returnId: string): Promise<void> {
  const response = await postReturnId('/order/return/submit', returnId);
  return unwrapApiResponse<void>(response.data);
}

export async function approveReturnOrder(returnId: string): Promise<void> {
  const response = await postReturnId('/order/return/approve', returnId);
  return unwrapApiResponse<void>(response.data);
}

export async function rejectReturnOrder(returnId: string): Promise<void> {
  const response = await postReturnId('/order/return/reject', returnId);
  return unwrapApiResponse<void>(response.data);
}

export async function confirmReturnReceive(returnId: string): Promise<void> {
  const response = await postReturnId('/order/return/receive', returnId);
  return unwrapApiResponse<void>(response.data);
}

export async function processReturnQc(payload: ReturnQcPayload): Promise<void> {
  const response = await apiClient.post('/order/return/qc', normalizeReturnQcPayload(payload));
  return unwrapApiResponse<void>(response.data);
}

function normalizeReturnOrderPayload(payload: CreateReturnOrderPayload): Record<string, unknown> {
  return normalizeOptionalFields({
    ...payload,
    lines: payload.lines.map((line) => normalizeOptionalFields(line)),
  }) as Record<string, unknown>;
}

function normalizeReturnQcPayload(payload: ReturnQcPayload): Record<string, unknown> {
  return normalizeOptionalFields({
    returnId: payload.returnId,
    results: payload.results.map((result) => normalizeOptionalFields(result)),
  }) as Record<string, unknown>;
}

function postReturnId(path: string, returnId: string) {
  return apiClient.post(path, null, { params: { returnId: returnId.trim() } });
}

function normalizePageQuery<T extends { current: number; size: number }>(params: T): Partial<T> {
  return normalizeOptionalFields({ ...params, current: Math.max(1, params.current), size: Math.max(1, params.size) });
}
