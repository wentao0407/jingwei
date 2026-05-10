import { apiClient, unwrapApiResponse } from '@/services/http/apiClient';

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

export async function createReturnOrder(payload: CreateReturnOrderPayload): Promise<ReturnOrderRecord> {
  const response = await apiClient.post('/order/return/create', normalizeReturnOrderPayload(payload));
  return unwrapApiResponse<ReturnOrderRecord>(response.data);
}

export async function submitReturnOrder(returnId: string): Promise<void> {
  const response = await apiClient.post('/order/return/submit', null, { params: { returnId } });
  return unwrapApiResponse<void>(response.data);
}

function normalizeReturnOrderPayload(payload: CreateReturnOrderPayload): Record<string, unknown> {
  return normalizeOptionalFields({
    ...payload,
    lines: payload.lines.map((line) => normalizeOptionalFields(line)),
  }) as Record<string, unknown>;
}

function normalizeOptionalFields<T extends object>(payload: T): Partial<T> {
  return Object.fromEntries(
    Object.entries(payload)
      .map(([key, value]) => [key, typeof value === 'string' ? value.trim() : value])
      .filter(([, value]) => value !== undefined && value !== null && value !== ''),
  ) as Partial<T>;
}
