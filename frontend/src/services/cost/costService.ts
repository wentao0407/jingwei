import { apiClient, unwrapApiResponse } from '@/services/http/apiClient';

export interface CostDetailQuery {
  productionOrderId: string;
  productionLineId: string;
}

export interface CostProductionOrderRecord {
  id: string;
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

export interface CostMaterialIssueRecord {
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

export async function getCostDetail(params: CostDetailQuery): Promise<CostProductionOrderRecord> {
  const response = await apiClient.post('/cost/detail', null, {
    params: {
      productionOrderId: params.productionOrderId.trim(),
      productionLineId: params.productionLineId.trim(),
    },
  });
  return unwrapApiResponse<CostProductionOrderRecord>(response.data);
}

export async function getCostIssueDetails(productionOrderId: string): Promise<CostMaterialIssueRecord[]> {
  const response = await apiClient.post('/cost/issues', null, {
    params: { productionOrderId: productionOrderId.trim() },
  });
  return unwrapApiResponse<CostMaterialIssueRecord[]>(response.data);
}
