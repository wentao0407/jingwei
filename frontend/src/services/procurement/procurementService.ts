import { apiClient, unwrapApiResponse } from '@/services/http/apiClient';
import type { PageResult } from '@/services/master/customerService';

export interface ProcurementOrderQueryParams {
  current: number;
  size: number;
  supplierId?: string;
  status?: string;
}

export interface ProcurementOrderLineRecord {
  id: string;
  lineNo?: number | null;
  materialId?: string | null;
  materialCode?: string | null;
  materialName?: string | null;
  materialType?: string | null;
  quantity?: number | null;
  unit?: string | null;
  unitPrice?: number | null;
  lineAmount?: number | null;
  deliveredQuantity?: number | null;
  acceptedQuantity?: number | null;
  rejectedQuantity?: number | null;
  mrpResultId?: string | null;
  remark?: string | null;
}

export interface ProcurementOrderRecord {
  id: string;
  orderNo: string;
  supplierId?: string | null;
  supplierName?: string | null;
  orderDate?: string | null;
  expectedDeliveryDate?: string | null;
  status?: string | null;
  statusLabel?: string | null;
  totalAmount?: number | null;
  paidAmount?: number | null;
  paymentStatus?: string | null;
  mrpBatchNo?: string | null;
  remark?: string | null;
  createdAt?: string | null;
  updatedAt?: string | null;
  lines?: ProcurementOrderLineRecord[] | null;
}

export interface FireProcurementOrderEventPayload {
  orderId: string;
  event: string;
}

export interface AsnQueryParams {
  current: number;
  size: number;
  procurementOrderId?: string;
  status?: string;
}

export interface AsnLineRecord {
  id: string;
  asnId?: string | null;
  procurementLineId?: string | null;
  materialId?: string | null;
  materialCode?: string | null;
  materialName?: string | null;
  expectedQuantity?: number | null;
  receivedQuantity?: number | null;
  qcStatus?: string | null;
  qcStatusLabel?: string | null;
  acceptedQuantity?: number | null;
  rejectedQuantity?: number | null;
  batchNo?: string | null;
  remark?: string | null;
}

export interface AsnRecord {
  id: string;
  asnNo: string;
  procurementOrderId?: string | null;
  procurementOrderNo?: string | null;
  supplierId?: string | null;
  supplierName?: string | null;
  expectedArrivalDate?: string | null;
  actualArrivalDate?: string | null;
  status?: string | null;
  statusLabel?: string | null;
  receiverId?: string | null;
  remark?: string | null;
  createdAt?: string | null;
  updatedAt?: string | null;
  lines?: AsnLineRecord[] | null;
}

export interface ReceiveAsnGoodsPayload {
  asnId: string;
  lines: Array<{ lineId: string; receivedQuantity: number }>;
}

export interface SubmitAsnQcPayload {
  lineId: string;
  acceptedQuantity: number;
  rejectedQuantity: number;
  inspector?: string;
  conclusion?: string;
}

export interface BomQueryParams {
  current: number;
  size: number;
  spuId?: string;
  status?: string;
}

export interface BomItemRecord {
  id: string;
  materialId?: string | null;
  materialCode?: string | null;
  materialName?: string | null;
  materialType?: string | null;
  consumptionType?: string | null;
  consumptionTypeLabel?: string | null;
  baseConsumption?: number | null;
  unit?: string | null;
  wastageRate?: number | null;
  sortOrder?: number | null;
  remark?: string | null;
}

export interface BomRecord {
  id: string;
  code: string;
  spuId?: string | null;
  spuCode?: string | null;
  spuName?: string | null;
  bomVersion?: number | null;
  status?: string | null;
  statusLabel?: string | null;
  effectiveFrom?: string | null;
  effectiveTo?: string | null;
  remark?: string | null;
  items?: BomItemRecord[] | null;
}

export interface MrpCalculatePayload {
  productionOrderIds?: string[];
}

export interface MrpCalculateResultRecord {
  batchNo: string;
  totalItems?: number | null;
}

export interface MrpResultQueryParams {
  current: number;
  size: number;
  batchNo?: string;
  materialId?: string;
  status?: string;
}

export interface MrpResultRecord {
  id: string;
  batchNo?: string | null;
  materialCode?: string | null;
  materialName?: string | null;
  grossDemand?: number | null;
  allocatedStock?: number | null;
  inTransitQuantity?: number | null;
  netDemand?: number | null;
  suggestedQuantity?: number | null;
  unit?: string | null;
  suggestedSupplierName?: string | null;
  estimatedCost?: number | null;
  status?: string | null;
  statusLabel?: string | null;
}

export async function pageProcurementOrders(
  params: ProcurementOrderQueryParams,
): Promise<PageResult<ProcurementOrderRecord>> {
  const response = await apiClient.post('/procurement/order/page', normalizePageQuery(params));
  return unwrapApiResponse<PageResult<ProcurementOrderRecord>>(response.data);
}

export async function getProcurementOrderDetail(orderId: string): Promise<ProcurementOrderRecord> {
  const response = await apiClient.post('/procurement/order/detail', null, { params: { orderId } });
  return unwrapApiResponse<ProcurementOrderRecord>(response.data);
}

export async function getProcurementOrderAvailableActions(orderId: string): Promise<string[]> {
  const response = await apiClient.post('/procurement/order/available-actions', null, { params: { orderId } });
  return unwrapApiResponse<string[]>(response.data);
}

export async function fireProcurementOrderEvent(payload: FireProcurementOrderEventPayload): Promise<void> {
  const response = await apiClient.post('/procurement/order/fire-event', normalizeOptionalFields(payload));
  return unwrapApiResponse<void>(response.data);
}

export async function pageAsns(params: AsnQueryParams): Promise<PageResult<AsnRecord>> {
  const response = await apiClient.post('/procurement/asn/page', normalizePageQuery(params));
  return unwrapApiResponse<PageResult<AsnRecord>>(response.data);
}

export async function getAsnDetail(asnId: string): Promise<AsnRecord> {
  const response = await apiClient.post('/procurement/asn/detail', null, { params: { asnId } });
  return unwrapApiResponse<AsnRecord>(response.data);
}

export async function receiveAsnGoods(payload: ReceiveAsnGoodsPayload): Promise<void> {
  const response = await apiClient.post('/procurement/asn/receive', payload);
  return unwrapApiResponse<void>(response.data);
}

export async function submitAsnQc(payload: SubmitAsnQcPayload): Promise<void> {
  const response = await apiClient.post('/procurement/asn/qc', normalizeOptionalFields(payload));
  return unwrapApiResponse<void>(response.data);
}

export async function pageBoms(params: BomQueryParams): Promise<PageResult<BomRecord>> {
  const response = await apiClient.post('/procurement/bom/page', normalizePageQuery(params));
  return unwrapApiResponse<PageResult<BomRecord>>(response.data);
}

export async function getBomDetail(bomId: string): Promise<BomRecord> {
  const response = await apiClient.post('/procurement/bom/detail', null, { params: { bomId } });
  return unwrapApiResponse<BomRecord>(response.data);
}

export async function approveBom(bomId: string): Promise<void> {
  const response = await apiClient.post('/procurement/bom/approve', null, { params: { bomId } });
  return unwrapApiResponse<void>(response.data);
}

export async function calculateMrp(payload: MrpCalculatePayload): Promise<MrpCalculateResultRecord> {
  const response = await apiClient.post('/procurement/mrp/calculate', normalizeMrpPayload(payload));
  return unwrapApiResponse<MrpCalculateResultRecord>(response.data);
}

export async function pageMrpResults(params: MrpResultQueryParams): Promise<PageResult<MrpResultRecord>> {
  const response = await apiClient.post('/procurement/mrp/results', normalizePageQuery(params));
  return unwrapApiResponse<PageResult<MrpResultRecord>>(response.data);
}

function normalizePageQuery<T extends { current: number; size: number }>(params: T): Partial<T> {
  return normalizeOptionalFields({
    ...params,
    current: Math.max(1, params.current),
    size: Math.max(1, params.size),
  });
}

function normalizeMrpPayload(payload: MrpCalculatePayload): MrpCalculatePayload {
  const productionOrderIds = (payload.productionOrderIds ?? [])
    .map((id) => id.trim())
    .filter(Boolean);
  return productionOrderIds.length > 0 ? { productionOrderIds } : {};
}

function normalizeOptionalFields<T extends object>(payload: T): Partial<T> {
  return Object.fromEntries(
    Object.entries(payload)
      .map(([key, value]) => [key, typeof value === 'string' ? value.trim() : value])
      .filter(([, value]) => value !== undefined && value !== null && value !== ''),
  ) as Partial<T>;
}
