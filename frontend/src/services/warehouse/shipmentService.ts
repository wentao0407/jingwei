import { apiClient, unwrapApiResponse } from '@/services/http/apiClient';
import type { OutboundOrderRecord } from '@/services/inventory/inventoryService';
import type { PageResult } from '@/services/master/customerService';
import { normalizeOptionalFields } from '@/services/shared/normalize';

export interface ConfirmShipmentPayload {
  outboundId: string;
  salesOrderId?: string;
}

export interface ShipmentQueryParams {
  current: number;
  size: number;
  status?: string;
  warehouseId?: string;
  outboundNo?: string;
}

export async function confirmShipment(payload: ConfirmShipmentPayload): Promise<void> {
  const response = await apiClient.post('/warehouse/shipment/confirm', normalizeOptionalFields(payload));
  return unwrapApiResponse<void>(response.data);
}

export async function pageShipments(params: ShipmentQueryParams): Promise<PageResult<OutboundOrderRecord>> {
  const response = await apiClient.post('/warehouse/shipment/page', normalizeShipmentQuery(params));
  return unwrapApiResponse<PageResult<OutboundOrderRecord>>(response.data);
}

export async function getShipmentDetail(shipmentId: string): Promise<OutboundOrderRecord> {
  const response = await apiClient.post('/warehouse/shipment/detail', null, {
    params: { shipmentId },
  });
  return unwrapApiResponse<OutboundOrderRecord>(response.data);
}

function normalizeShipmentQuery(params: ShipmentQueryParams): ShipmentQueryParams {
  return normalizeOptionalFields({
    ...params,
    current: Math.max(1, params.current),
    size: Math.max(1, params.size),
  }) as ShipmentQueryParams;
}
