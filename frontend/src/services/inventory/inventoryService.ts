import { apiClient, unwrapApiResponse } from '@/services/http/apiClient';
import type { PageResult } from '@/services/master/customerService';
import { normalizeOptionalFields } from '@/services/shared/normalize';

export interface InventoryOrderLineRecord {
  id?: string;
  lineNo?: number | null;
  inventoryType?: string | null;
  skuId?: string | null;
  skuCode?: string | null;
  materialId?: string | null;
  materialName?: string | null;
  batchNo?: string | null;
  plannedQty?: number | null;
  actualQty?: number | null;
  locationId?: string | null;
  locationCode?: string | null;
  unitCost?: number | null;
  allocationId?: string | null;
  remark?: string | null;
}

export interface InboundOrderRecord {
  id: string;
  inboundNo?: string | null;
  inboundType?: string | null;
  inboundTypeLabel?: string | null;
  warehouseId?: string | null;
  warehouseName?: string | null;
  status?: string | null;
  statusLabel?: string | null;
  sourceType?: string | null;
  sourceId?: string | null;
  sourceNo?: string | null;
  inboundDate?: string | null;
  remark?: string | null;
  lines?: InventoryOrderLineRecord[] | null;
}

export interface OutboundOrderRecord {
  id: string;
  outboundNo?: string | null;
  outboundType?: string | null;
  outboundTypeLabel?: string | null;
  warehouseId?: string | null;
  warehouseName?: string | null;
  status?: string | null;
  statusLabel?: string | null;
  sourceType?: string | null;
  sourceId?: string | null;
  sourceNo?: string | null;
  outboundDate?: string | null;
  carrier?: string | null;
  trackingNo?: string | null;
  remark?: string | null;
  lines?: InventoryOrderLineRecord[] | null;
}

export interface StocktakingLineRecord {
  id: string;
  inventoryType?: string | null;
  skuId?: string | null;
  skuCode?: string | null;
  materialId?: string | null;
  materialName?: string | null;
  warehouseId?: string | null;
  locationId?: string | null;
  locationCode?: string | null;
  batchNo?: string | null;
  systemQty?: number | null;
  actualQty?: number | null;
  diffQty?: number | null;
  diffStatus?: string | null;
  needRecheck?: boolean | null;
}

export interface StocktakingOrderRecord {
  id: string;
  stocktakingNo?: string | null;
  stocktakingType?: string | null;
  stocktakingTypeLabel?: string | null;
  countMode?: string | null;
  countModeLabel?: string | null;
  warehouseId?: string | null;
  warehouseName?: string | null;
  zoneCode?: string | null;
  status?: string | null;
  statusLabel?: string | null;
  plannedDate?: string | null;
  remark?: string | null;
  lines?: StocktakingLineRecord[] | null;
}

export interface InventoryStockRecord {
  id: string;
  warehouseId?: string | null;
  warehouseName?: string | null;
  locationId?: string | null;
  locationCode?: string | null;
  batchNo?: string | null;
  availableQty?: number | null;
  lockedQty?: number | null;
  qcQty?: number | null;
  totalQty?: number | null;
  inTransitQty?: number | null;
}

export interface InventorySkuRecord extends InventoryStockRecord {
  skuId?: string | null;
  skuCode?: string | null;
}

export interface InventoryMaterialRecord extends InventoryStockRecord {
  materialId?: string | null;
  materialCode?: string | null;
  materialName?: string | null;
}

interface PageQuery {
  current: number;
  size: number;
  status?: string;
  warehouseId?: string;
  inboundNo?: string;
  outboundNo?: string;
}

export type InboundQueryParams = PageQuery;
export type OutboundQueryParams = PageQuery;
export type StocktakingQueryParams = Omit<PageQuery, 'inboundNo' | 'outboundNo'>;
export type InventorySkuQueryParams = Omit<PageQuery, 'status' | 'inboundNo' | 'outboundNo'> & { skuId?: string; batchNo?: string };
export type InventoryMaterialQueryParams = Omit<PageQuery, 'status' | 'inboundNo' | 'outboundNo'> & {
  materialId?: string;
  batchNo?: string;
};

export interface CreateInboundPayload {
  inboundType: string;
  warehouseId: string;
  lines: InventoryOrderLineRecord[];
}

export interface CreateOutboundPayload {
  outboundType: string;
  warehouseId: string;
  lines: InventoryOrderLineRecord[];
}

export interface CreateStocktakingPayload {
  stocktakingType: string;
  countMode: string;
  warehouseId: string;
}

export interface RecordStocktakingCountPayload {
  stocktakingId: string;
  lineId: string;
  actualQty: number;
}

export async function pageInboundOrders(params: InboundQueryParams): Promise<PageResult<InboundOrderRecord>> {
  const response = await apiClient.post('/inventory/inbound/page', normalizePageQuery(params));
  return unwrapApiResponse<PageResult<InboundOrderRecord>>(response.data);
}

export async function getInboundDetail(inboundId: string): Promise<InboundOrderRecord> {
  const response = await apiClient.post('/inventory/inbound/detail', null, { params: { inboundId } });
  return unwrapApiResponse<InboundOrderRecord>(response.data);
}

export async function confirmInbound(inboundId: string): Promise<void> {
  const response = await apiClient.post('/inventory/inbound/confirm', null, { params: { inboundId } });
  return unwrapApiResponse<void>(response.data);
}

export async function createInboundOrder(payload: CreateInboundPayload): Promise<InboundOrderRecord> {
  const response = await apiClient.post('/inventory/inbound/create', normalizeOptionalFields(payload));
  return unwrapApiResponse<InboundOrderRecord>(response.data);
}

export async function pageOutboundOrders(params: OutboundQueryParams): Promise<PageResult<OutboundOrderRecord>> {
  const response = await apiClient.post('/inventory/outbound/page', normalizePageQuery(params));
  return unwrapApiResponse<PageResult<OutboundOrderRecord>>(response.data);
}

export async function getOutboundDetail(outboundId: string): Promise<OutboundOrderRecord> {
  const response = await apiClient.post('/inventory/outbound/detail', null, { params: { outboundId } });
  return unwrapApiResponse<OutboundOrderRecord>(response.data);
}

export async function confirmOutbound(outboundId: string): Promise<void> {
  const response = await apiClient.post('/inventory/outbound/confirm', null, { params: { outboundId } });
  return unwrapApiResponse<void>(response.data);
}

export async function createOutboundOrder(payload: CreateOutboundPayload): Promise<OutboundOrderRecord> {
  const response = await apiClient.post('/inventory/outbound/create', normalizeOptionalFields(payload));
  return unwrapApiResponse<OutboundOrderRecord>(response.data);
}

export async function pageStocktakingOrders(
  params: StocktakingQueryParams,
): Promise<PageResult<StocktakingOrderRecord>> {
  const response = await apiClient.post('/inventory/stocktaking/page', normalizePageQuery(params));
  return unwrapApiResponse<PageResult<StocktakingOrderRecord>>(response.data);
}

export async function pageInventorySkus(params: InventorySkuQueryParams): Promise<PageResult<InventorySkuRecord>> {
  const response = await apiClient.post('/inventory/sku/page', normalizePageQuery(params));
  return unwrapApiResponse<PageResult<InventorySkuRecord>>(response.data);
}

export async function pageInventoryMaterials(
  params: InventoryMaterialQueryParams,
): Promise<PageResult<InventoryMaterialRecord>> {
  const response = await apiClient.post('/inventory/material/page', normalizePageQuery(params));
  return unwrapApiResponse<PageResult<InventoryMaterialRecord>>(response.data);
}

export async function getStocktakingDetail(stocktakingId: string): Promise<StocktakingOrderRecord> {
  const response = await apiClient.post('/inventory/stocktaking/detail', null, { params: { stocktakingId } });
  return unwrapApiResponse<StocktakingOrderRecord>(response.data);
}

export async function createStocktakingOrder(payload: CreateStocktakingPayload): Promise<StocktakingOrderRecord> {
  const response = await apiClient.post('/inventory/stocktaking/create', normalizeOptionalFields(payload));
  return unwrapApiResponse<StocktakingOrderRecord>(response.data);
}

export async function startStocktaking(stocktakingId: string): Promise<void> {
  const response = await apiClient.post('/inventory/stocktaking/start', null, { params: { stocktakingId } });
  return unwrapApiResponse<void>(response.data);
}

export async function recordStocktakingCount(payload: RecordStocktakingCountPayload): Promise<void> {
  const response = await apiClient.post('/inventory/stocktaking/record-count', normalizeOptionalFields(payload));
  return unwrapApiResponse<void>(response.data);
}

export async function submitStocktaking(stocktakingId: string): Promise<void> {
  const response = await apiClient.post('/inventory/stocktaking/submit', null, { params: { stocktakingId } });
  return unwrapApiResponse<void>(response.data);
}

export async function reviewStocktaking(stocktakingId: string): Promise<void> {
  const response = await apiClient.post('/inventory/stocktaking/review', null, { params: { stocktakingId } });
  return unwrapApiResponse<void>(response.data);
}

function normalizePageQuery<T extends { current: number; size: number }>(params: T): Partial<T> {
  return normalizeOptionalFields({ ...params, current: Math.max(1, params.current), size: Math.max(1, params.size) });
}
