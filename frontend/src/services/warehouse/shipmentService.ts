import { apiClient, unwrapApiResponse } from '@/services/http/apiClient';

export interface ConfirmShipmentPayload {
  outboundId: string;
  salesOrderId?: string;
}

export async function confirmShipment(payload: ConfirmShipmentPayload): Promise<void> {
  const response = await apiClient.post('/warehouse/shipment/confirm', normalizeOptionalFields(payload));
  return unwrapApiResponse<void>(response.data);
}

function normalizeOptionalFields<T extends object>(value: T): Partial<T> {
  return Object.fromEntries(
    Object.entries(value)
      .map(([key, fieldValue]) => [key, typeof fieldValue === 'string' ? fieldValue.trim() : fieldValue])
      .filter(([, fieldValue]) => fieldValue !== undefined && fieldValue !== null && fieldValue !== ''),
  ) as Partial<T>;
}
