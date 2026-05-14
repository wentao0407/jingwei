import { apiClient, unwrapApiResponse } from '@/services/http/apiClient';
import type { PageResult } from '@/services/master/customerService';

export interface ReportPageQuery {
  current: number;
  size: number;
  inventoryType?: string;
  warehouseId?: string;
  categoryId?: string;
  seasonId?: string;
  keyword?: string;
}

export interface InventoryLedgerRecord {
  inventoryId: string;
  skuCode?: string | null;
  materialCode?: string | null;
  materialName?: string | null;
  warehouseName?: string | null;
  batchNo?: string | null;
  availableQty?: number | null;
  lockedQty?: number | null;
  totalQty?: number | null;
  totalAmount?: number | null;
}

export interface OperationFlowRecord {
  id: string;
  operationNo?: string | null;
  operationTypeLabel?: string | null;
  inventoryType?: string | null;
  skuCode?: string | null;
  materialCode?: string | null;
  warehouseName?: string | null;
  changeQty?: number | null;
  operatedAt?: string | null;
}

export interface InventoryAgeRecord {
  inventoryId: string;
  skuCode?: string | null;
  materialCode?: string | null;
  warehouseName?: string | null;
  totalQty?: number | null;
  ageDays?: number | null;
  ageRange?: string | null;
  overdue?: boolean | null;
}

export interface InventoryAgeSummary {
  totalCount?: number | null;
  totalQty?: number | null;
  totalAmount?: number | null;
  overdueCount?: number | null;
  overdueQty?: number | null;
  details?: InventoryAgeRecord[] | null;
}

export interface TurnoverRecord {
  skuCode?: string | null;
  materialCode?: string | null;
  warehouseName?: string | null;
  currentQty?: number | null;
  outboundQty?: number | null;
  turnoverDays?: number | null;
  turnoverGradeLabel?: string | null;
}

export interface OperationFlowQuery extends ReportPageQuery {
  operationNo?: string;
  operationType?: string;
}

export interface TurnoverQuery extends ReportPageQuery {
  startDate?: string;
  endDate?: string;
}

export async function pageInventoryLedger(
  params: ReportPageQuery,
): Promise<PageResult<InventoryLedgerRecord>> {
  const response = await apiClient.post('/report/ledger/page', normalizePageQuery(params));
  return unwrapApiResponse<PageResult<InventoryLedgerRecord>>(response.data);
}

export async function pageOperationFlows(
  params: OperationFlowQuery,
): Promise<PageResult<OperationFlowRecord>> {
  const response = await apiClient.post('/report/flow/page', normalizePageQuery(params));
  return unwrapApiResponse<PageResult<OperationFlowRecord>>(response.data);
}

export async function queryInventoryAge(params: ReportPageQuery): Promise<InventoryAgeSummary> {
  const response = await apiClient.post('/report/age/summary', normalizePageQuery(params));
  return unwrapApiResponse<InventoryAgeSummary>(response.data);
}

export async function queryTurnoverAnalysis(params: TurnoverQuery): Promise<PageResult<TurnoverRecord>> {
  const response = await apiClient.post('/report/turnover/page', normalizePageQuery(params));
  return unwrapApiResponse<PageResult<TurnoverRecord>>(response.data);
}

function normalizePageQuery<T extends { current: number; size: number }>(params: T): Partial<T> {
  return normalizeOptionalFields({
    ...params,
    current: Math.max(1, params.current),
    size: Math.max(1, params.size),
  });
}

function normalizeOptionalFields<T extends object>(value: T): Partial<T> {
  return Object.fromEntries(
    Object.entries(value)
      .map(([key, fieldValue]) => [key, typeof fieldValue === 'string' ? fieldValue.trim() : fieldValue])
      .filter(([, fieldValue]) => fieldValue !== undefined && fieldValue !== null && fieldValue !== ''),
  ) as Partial<T>;
}
