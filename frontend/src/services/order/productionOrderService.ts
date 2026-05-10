import { apiClient, unwrapApiResponse } from '@/services/http/apiClient';
import type { PageResult } from '@/services/master/customerService';

export interface ProductionOrderQueryParams {
  current: number;
  size: number;
  status?: string;
  orderNo?: string;
  planDateStart?: string;
  planDateEnd?: string;
}

export interface ProductionOrderLineRecord {
  id: string;
  lineNo?: number | null;
  spuId?: string | null;
  spuCode?: string | null;
  spuName?: string | null;
  colorWayId?: string | null;
  colorName?: string | null;
  colorCode?: string | null;
  bomId?: string | null;
  sizeMatrix?: Record<string, unknown> | null;
  totalQuantity?: number | null;
  completedQuantity?: number | null;
  stockedQuantity?: number | null;
  skipCutting?: boolean | null;
  status?: string | null;
  statusLabel?: string | null;
  remark?: string | null;
}

export interface ProductionOrderRecord {
  id: string;
  orderNo: string;
  planDate?: string | null;
  deadlineDate?: string | null;
  status?: string | null;
  statusLabel?: string | null;
  sourceType?: string | null;
  workshopId?: string | null;
  totalQuantity?: number | null;
  completedQuantity?: number | null;
  stockedQuantity?: number | null;
  remark?: string | null;
  lines?: ProductionOrderLineRecord[] | null;
  createdAt?: string | null;
  updatedAt?: string | null;
}

export interface ProductionActionRecord {
  event: string;
  label: string;
  description?: string | null;
  targetStatus?: string | null;
}

export interface FireProductionOrderEventPayload {
  orderId: string;
  event: string;
}

export interface FireProductionLineEventPayload extends FireProductionOrderEventPayload {
  lineId: string;
}

export async function pageProductionOrders(
  params: ProductionOrderQueryParams,
): Promise<PageResult<ProductionOrderRecord>> {
  const response = await apiClient.post('/order/production/page', normalizeQuery(params));
  return unwrapApiResponse<PageResult<ProductionOrderRecord>>(response.data);
}

export async function getProductionOrderDetail(orderId: string): Promise<ProductionOrderRecord> {
  const response = await apiClient.post('/order/production/detail', null, { params: { orderId } });
  return unwrapApiResponse<ProductionOrderRecord>(response.data);
}

export async function getProductionOrderAvailableActions(orderId: string): Promise<ProductionActionRecord[]> {
  const response = await apiClient.post('/order/production/available-actions', null, { params: { orderId } });
  return unwrapApiResponse<ProductionActionRecord[]>(response.data);
}

export async function getProductionLineAvailableActions(
  orderId: string,
  lineId: string,
): Promise<ProductionActionRecord[]> {
  const response = await apiClient.post('/order/production/line-available-actions', null, {
    params: { orderId, lineId },
  });
  return unwrapApiResponse<ProductionActionRecord[]>(response.data);
}

export async function fireProductionOrderEvent(payload: FireProductionOrderEventPayload): Promise<void> {
  const response = await apiClient.post('/order/production/fire-event', normalizeOptionalFields(payload));
  return unwrapApiResponse<void>(response.data);
}

export async function fireProductionLineEvent(payload: FireProductionLineEventPayload): Promise<void> {
  const response = await apiClient.post('/order/production/fire-line-event', normalizeOptionalFields(payload));
  return unwrapApiResponse<void>(response.data);
}

function normalizeQuery(params: ProductionOrderQueryParams): ProductionOrderQueryParams {
  return normalizeOptionalFields({
    ...params,
    current: Math.max(1, params.current),
    size: Math.max(1, params.size),
  }) as ProductionOrderQueryParams;
}

function normalizeOptionalFields<T extends object>(payload: T): Partial<T> {
  return Object.fromEntries(
    Object.entries(payload)
      .map(([key, value]) => [key, typeof value === 'string' ? value.trim() : value])
      .filter(([, value]) => value !== undefined && value !== null && value !== ''),
  ) as Partial<T>;
}
