import { apiClient, unwrapApiResponse } from '@/services/http/apiClient';
import type { PageResult } from '@/services/master/customerService';
import { normalizeOptionalFields } from '@/services/shared/normalize';

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

export interface MrpCalculateResultRecord {
  batchNo: string;
  totalItems?: number | null;
}

export interface MaterialRequirementQueryParams {
  current: number;
  size: number;
  batchNo?: string;
  materialId?: string;
  status?: string;
}

export interface MaterialRequirementRecord {
  id: string;
  batchNo?: string | null;
  materialId?: string | null;
  materialCode?: string | null;
  materialName?: string | null;
  materialType?: string | null;
  grossDemand?: number | null;
  allocatedStock?: number | null;
  inTransitQuantity?: number | null;
  netDemand?: number | null;
  suggestedQuantity?: number | null;
  unit?: string | null;
  suggestedSupplierId?: string | null;
  suggestedSupplierName?: string | null;
  estimatedCost?: number | null;
  status?: string | null;
  statusLabel?: string | null;
  snapshotTime?: string | null;
  remark?: string | null;
}

export interface ProductionOrderCostRecord {
  id?: string | null;
  productionOrderId?: string | null;
  productionLineId?: string | null;
  materialCost?: number | null;
  trimCost?: number | null;
  packagingCost?: number | null;
  totalCost?: number | null;
  completedQty?: number | null;
  unitCost?: number | null;
  updatedAt?: string | null;
}

export interface ProductionOrderCostIssueRecord {
  id: string;
  productionOrderId?: string | null;
  productionLineId?: string | null;
  materialId?: string | null;
  materialType?: string | null;
  materialTypeLabel?: string | null;
  issueQty?: number | null;
  unitCost?: number | null;
  costAmount?: number | null;
  issueDate?: string | null;
  createdAt?: string | null;
}

export interface FireProductionOrderEventPayload {
  orderId: string;
  event: string;
}

export interface FireProductionLineEventPayload extends FireProductionOrderEventPayload {
  lineId: string;
}

export interface ProductionOrderSizePayload {
  sizeId: string;
  code: string;
  quantity?: number;
}

export interface SaveProductionOrderLinePayload {
  spuId: string;
  colorWayId: string;
  sizeGroupId: string;
  sizes: ProductionOrderSizePayload[];
  bomId?: string;
  skipCutting?: boolean;
  remark?: string;
}

export interface SaveProductionOrderPayload {
  planDate?: string;
  deadlineDate?: string;
  sourceType?: string;
  workshopId?: string;
  remark?: string;
  lines: SaveProductionOrderLinePayload[];
}

export async function pageProductionOrders(
  params: ProductionOrderQueryParams,
): Promise<PageResult<ProductionOrderRecord>> {
  const response = await apiClient.post('/order/production/page', normalizeQuery(params));
  return unwrapApiResponse<PageResult<ProductionOrderRecord>>(response.data);
}

export async function createProductionOrder(payload: SaveProductionOrderPayload): Promise<ProductionOrderRecord> {
  const response = await apiClient.post('/order/production/create', normalizeProductionOrderPayload(payload));
  return unwrapApiResponse<ProductionOrderRecord>(response.data);
}

export async function updateProductionOrder(
  orderId: string,
  payload: SaveProductionOrderPayload,
): Promise<ProductionOrderRecord> {
  const response = await apiClient.post('/order/production/update', normalizeProductionOrderPayload(payload), {
    params: { orderId: orderId.trim() },
  });
  return unwrapApiResponse<ProductionOrderRecord>(response.data);
}

export async function deleteProductionOrder(orderId: string): Promise<void> {
  const response = await apiClient.post('/order/production/delete', null, {
    params: { orderId: orderId.trim() },
  });
  return unwrapApiResponse<void>(response.data);
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

export async function calculateProductionOrderMaterialRequirements(
  productionOrderId: string,
): Promise<MrpCalculateResultRecord> {
  const response = await apiClient.post('/procurement/mrp/calculate', {
    productionOrderIds: [productionOrderId.trim()],
  });
  return unwrapApiResponse<MrpCalculateResultRecord>(response.data);
}

export async function pageProductionOrderMaterialRequirements(
  params: MaterialRequirementQueryParams,
): Promise<PageResult<MaterialRequirementRecord>> {
  const response = await apiClient.post('/procurement/mrp/results', normalizeMaterialRequirementQuery(params));
  return unwrapApiResponse<PageResult<MaterialRequirementRecord>>(response.data);
}

export async function getProductionOrderCostDetail(
  productionOrderId: string,
  productionLineId: string,
): Promise<ProductionOrderCostRecord> {
  const response = await apiClient.post('/cost/detail', null, {
    params: { productionOrderId, productionLineId },
  });
  return unwrapApiResponse<ProductionOrderCostRecord>(response.data);
}

export async function getProductionOrderCostIssues(
  productionOrderId: string,
): Promise<ProductionOrderCostIssueRecord[]> {
  const response = await apiClient.post('/cost/issues', null, { params: { productionOrderId } });
  return unwrapApiResponse<ProductionOrderCostIssueRecord[]>(response.data);
}

function normalizeQuery(params: ProductionOrderQueryParams): ProductionOrderQueryParams {
  return normalizeOptionalFields({
    ...params,
    current: Math.max(1, params.current),
    size: Math.max(1, params.size),
  }) as ProductionOrderQueryParams;
}

function normalizeMaterialRequirementQuery(params: MaterialRequirementQueryParams): MaterialRequirementQueryParams {
  return normalizeOptionalFields({
    ...params,
    current: Math.max(1, params.current),
    size: Math.max(1, params.size),
  }) as MaterialRequirementQueryParams;
}

function normalizeProductionOrderPayload(payload: SaveProductionOrderPayload): Record<string, unknown> {
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
