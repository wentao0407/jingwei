import { apiClient, unwrapApiResponse } from '@/services/http/apiClient';
import { normalizeOptionalFields } from '@/services/shared/normalize';

export interface InventoryAlertRecord {
  id: string;
  ruleId?: string | null;
  ruleName?: string | null;
  alertType?: string | null;
  alertTypeLabel?: string | null;
  inventoryType?: string | null;
  skuId?: string | null;
  skuCode?: string | null;
  materialId?: string | null;
  materialName?: string | null;
  warehouseId?: string | null;
  warehouseName?: string | null;
  currentValue?: number | null;
  thresholdValue?: number | null;
  status?: string | null;
  statusLabel?: string | null;
  acknowledgedBy?: string | null;
  acknowledgedAt?: string | null;
  resolvedAt?: string | null;
  createdAt?: string | null;
}

export interface AlertQueryParams {
  status?: string;
}

export interface AlertScanResult {
  created?: number;
  updated?: number;
  resolved?: number;
  [key: string]: number | undefined;
}

export async function listInventoryAlerts(params: AlertQueryParams = {}): Promise<InventoryAlertRecord[]> {
  const response = await apiClient.post('/inventory/alert/list', normalizeOptionalFields(params));
  return unwrapApiResponse<InventoryAlertRecord[]>(response.data);
}

export async function scanInventoryAlerts(): Promise<AlertScanResult> {
  const response = await apiClient.post('/inventory/alert/scan');
  return unwrapApiResponse<AlertScanResult>(response.data);
}

export async function acknowledgeAlert(alertId: string): Promise<void> {
  const response = await apiClient.post('/inventory/alert/acknowledge', null, { params: { alertId } });
  return unwrapApiResponse<void>(response.data);
}

