import { apiClient, unwrapApiResponse } from '@/services/http/apiClient';
import { normalizeOptionalFields } from '@/services/shared/normalize';

export interface ConfirmShipmentPayload {
  outboundId: string;
  salesOrderId?: string;
}

export async function confirmShipment(payload: ConfirmShipmentPayload): Promise<void> {
  const response = await apiClient.post('/warehouse/shipment/confirm', normalizeOptionalFields(payload));
  return unwrapApiResponse<void>(response.data);
}

