import { apiClient, unwrapApiResponse } from '@/services/http/apiClient';
import type { PageResult } from '@/services/master/customerService';
import { normalizeOptionalFields } from '@/services/shared/normalize';

export interface SalesOrderQueryParams {
  current: number;
  size: number;
  status?: string;
  customerId?: string;
  seasonId?: string;
  orderNo?: string;
  orderDateStart?: string;
  orderDateEnd?: string;
}

export interface SalesOrderLineRecord {
  id: string;
  lineNo?: number | null;
  spuId?: string | null;
  spuCode?: string | null;
  spuName?: string | null;
  colorWayId?: string | null;
  colorName?: string | null;
  colorCode?: string | null;
  sizeMatrix?: Record<string, unknown> | null;
  totalQuantity?: number | null;
  unitPrice?: number | null;
  lineAmount?: number | null;
  discountRate?: number | null;
  discountAmount?: number | null;
  actualAmount?: number | null;
  deliveryDate?: string | null;
  remark?: string | null;
}

export interface SalesOrderRecord {
  id: string;
  orderNo: string;
  customerId?: string | null;
  customerName?: string | null;
  customerLevel?: string | null;
  seasonId?: string | null;
  seasonName?: string | null;
  orderDate?: string | null;
  deliveryDate?: string | null;
  status: string;
  statusLabel?: string | null;
  totalQuantity?: number | null;
  totalAmount?: number | null;
  discountAmount?: number | null;
  actualAmount?: number | null;
  paymentStatus?: string | null;
  paymentAmount?: number | null;
  salesRepId?: string | null;
  remark?: string | null;
  lines?: SalesOrderLineRecord[] | null;
  createdAt?: string | null;
  updatedAt?: string | null;
}

export interface SalesOrderSizePayload {
  sizeId: string;
  code: string;
  quantity?: number;
}

export interface SalesOrderLinePayload {
  spuId: string;
  colorWayId: string;
  sizeGroupId: string;
  sizes: SalesOrderSizePayload[];
  unitPrice?: number;
  discountRate?: number;
  deliveryDate?: string;
  remark?: string;
}

export interface SaveSalesOrderPayload {
  customerId: string;
  seasonId?: string;
  orderDate?: string;
  deliveryDate?: string;
  salesRepId?: string;
  remark?: string;
  lines: SalesOrderLinePayload[];
}

export interface ConvertSalesOrderLinePayload {
  salesOrderLineId: string;
  skipCutting?: boolean;
}

export interface ConvertSalesOrderPayload {
  salesOrderId: string;
  lines: ConvertSalesOrderLinePayload[];
  workshopId?: string;
  deadlineDate?: string;
  remark?: string;
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

export interface ConvertSalesOrderResult {
  productionOrders?: ProductionOrderRecord[] | null;
  salesOrderId?: string | null;
  salesOrderNo?: string | null;
  salesOrderStatus?: string | null;
  salesOrderStatusLabel?: string | null;
}

export interface QuantityChangePayload {
  orderId: string;
  orderLineId: string;
  sizeGroupId: string;
  sizes: SalesOrderSizePayload[];
  reason: string;
}

export interface QuantityChangeRecord {
  id: string;
  orderId?: string | null;
  orderLineId?: string | null;
  sizeMatrixBefore?: unknown;
  sizeMatrixAfter?: unknown;
  diffMatrix?: unknown;
  reason?: string | null;
  status?: string | null;
  approvedBy?: string | null;
  approvedAt?: string | null;
  createdBy?: string | null;
  createdAt?: string | null;
}

export interface SalesOrderTimelineRecord {
  id: string;
  changeType?: string | null;
  fieldName?: string | null;
  oldValue?: string | null;
  newValue?: string | null;
  changeReason?: string | null;
  operatedBy?: string | null;
  operatedAt?: string | null;
}

export async function pageSalesOrders(params: SalesOrderQueryParams): Promise<PageResult<SalesOrderRecord>> {
  const response = await apiClient.post('/order/sales/page', normalizeQuery(params));
  return unwrapApiResponse<PageResult<SalesOrderRecord>>(response.data);
}

export async function createSalesOrder(payload: SaveSalesOrderPayload): Promise<SalesOrderRecord> {
  const response = await apiClient.post('/order/sales/create', normalizeSalesOrderPayload(payload));
  return unwrapApiResponse<SalesOrderRecord>(response.data);
}

export async function updateSalesOrder(orderId: string, payload: SaveSalesOrderPayload): Promise<SalesOrderRecord> {
  const response = await apiClient.post('/order/sales/update', normalizeSalesOrderPayload(payload), {
    params: { orderId },
  });
  return unwrapApiResponse<SalesOrderRecord>(response.data);
}

export async function convertSalesOrderToProduction(payload: ConvertSalesOrderPayload): Promise<ConvertSalesOrderResult> {
  const response = await apiClient.post('/order/sales/convert-to-production', normalizeConvertPayload(payload));
  return unwrapApiResponse<ConvertSalesOrderResult>(response.data);
}

export async function createQuantityChange(payload: QuantityChangePayload): Promise<QuantityChangeRecord> {
  const response = await apiClient.post('/order/sales/quantity-change', normalizeQuantityChangePayload(payload));
  return unwrapApiResponse<QuantityChangeRecord>(response.data);
}

export async function getSalesOrderTimeline(orderId: string): Promise<SalesOrderTimelineRecord[]> {
  const response = await apiClient.post('/order/sales/timeline', null, { params: { orderId: orderId.trim() } });
  return unwrapApiResponse<SalesOrderTimelineRecord[]>(response.data);
}

export async function listQuantityChanges(orderId: string): Promise<QuantityChangeRecord[]> {
  const response = await apiClient.post('/order/sales/quantity-change/list', null, { params: { orderId: orderId.trim() } });
  return unwrapApiResponse<QuantityChangeRecord[]>(response.data);
}

export async function getSalesOrderDetail(orderId: string): Promise<SalesOrderRecord> {
  const response = await apiClient.post('/order/sales/detail', null, { params: { orderId: orderId.trim() } });
  return unwrapApiResponse<SalesOrderRecord>(response.data);
}

export async function submitSalesOrder(orderId: string): Promise<void> {
  const response = await apiClient.post('/order/sales/submit', null, { params: { orderId } });
  return unwrapApiResponse<void>(response.data);
}

export async function resubmitSalesOrder(orderId: string): Promise<void> {
  const response = await apiClient.post('/order/sales/resubmit', null, { params: { orderId } });
  return unwrapApiResponse<void>(response.data);
}

export async function cancelSalesOrder(orderId: string): Promise<void> {
  const response = await apiClient.post('/order/sales/cancel', null, { params: { orderId } });
  return unwrapApiResponse<void>(response.data);
}

export async function deleteSalesOrder(orderId: string): Promise<void> {
  const response = await apiClient.post('/order/sales/delete', null, { params: { orderId } });
  return unwrapApiResponse<void>(response.data);
}

function normalizeQuery(params: SalesOrderQueryParams): SalesOrderQueryParams {
  return normalizeOptionalFields({
    ...params,
    current: Math.max(1, params.current),
    size: Math.max(1, params.size),
  }) as SalesOrderQueryParams;
}

function normalizeSalesOrderPayload(payload: SaveSalesOrderPayload): Record<string, unknown> {
  return normalizeOptionalFields({
    ...payload,
    lines: payload.lines.map((line) =>
      normalizeOptionalFields({
        ...line,
        sizes: line.sizes.map((size) => normalizeOptionalFields(size)),
      }),
    ),
  }) as Record<string, unknown>;
}

function normalizeConvertPayload(payload: ConvertSalesOrderPayload): Record<string, unknown> {
  return normalizeOptionalFields({
    ...payload,
    lines: payload.lines.map((line) => normalizeOptionalFields(line)),
  }) as Record<string, unknown>;
}

function normalizeQuantityChangePayload(payload: QuantityChangePayload): Record<string, unknown> {
  return normalizeOptionalFields({
    ...payload,
    sizes: payload.sizes.map((size) => normalizeOptionalFields(size)),
  }) as Record<string, unknown>;
}
