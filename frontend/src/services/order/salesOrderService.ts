import { apiClient, unwrapApiResponse } from '@/services/http/apiClient';
import type { PageResult } from '@/services/master/customerService';

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

export async function pageSalesOrders(params: SalesOrderQueryParams): Promise<PageResult<SalesOrderRecord>> {
  const response = await apiClient.post('/order/sales/page', normalizeQuery(params));
  return unwrapApiResponse<PageResult<SalesOrderRecord>>(response.data);
}

export async function getSalesOrderDetail(orderId: string): Promise<SalesOrderRecord> {
  const response = await apiClient.post('/order/sales/detail', null, { params: { orderId } });
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

function normalizeOptionalFields<T extends object>(payload: T): Partial<T> {
  return Object.fromEntries(
    Object.entries(payload)
      .map(([key, value]) => [key, typeof value === 'string' ? value.trim() : value])
      .filter(([, value]) => value !== undefined && value !== null && value !== ''),
  ) as Partial<T>;
}
