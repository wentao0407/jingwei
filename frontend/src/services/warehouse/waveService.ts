import { apiClient, unwrapApiResponse } from '@/services/http/apiClient';

export interface CreateWavePayload {
  warehouseId: string;
  strategy: string;
  outboundOrderIds: string[];
  remark?: string;
}

export interface ConfirmPickPayload {
  pickItemId: string;
  actualQty: number;
}

export async function createWave(payload: CreateWavePayload): Promise<string> {
  const response = await apiClient.post('/warehouse/wave/create', normalizeWavePayload(payload));
  return unwrapApiResponse<string>(response.data);
}

export async function confirmPick(payload: ConfirmPickPayload): Promise<void> {
  const response = await apiClient.post('/warehouse/wave/confirm-pick', normalizeOptionalFields(payload));
  return unwrapApiResponse<void>(response.data);
}

export async function completePickList(pickListId: string): Promise<void> {
  const response = await apiClient.post('/warehouse/wave/complete-pick-list', null, { params: { pickListId } });
  return unwrapApiResponse<void>(response.data);
}

export async function cancelWave(waveId: string): Promise<void> {
  const response = await apiClient.post('/warehouse/wave/cancel', null, { params: { waveId } });
  return unwrapApiResponse<void>(response.data);
}

function normalizeWavePayload(payload: CreateWavePayload): Partial<CreateWavePayload> {
  return normalizeOptionalFields({
    ...payload,
    outboundOrderIds: payload.outboundOrderIds.map((id) => id.trim()).filter(Boolean),
  });
}

function normalizeOptionalFields<T extends object>(value: T): Partial<T> {
  return Object.fromEntries(
    Object.entries(value)
      .map(([key, fieldValue]) => [key, typeof fieldValue === 'string' ? fieldValue.trim() : fieldValue])
      .filter(([, fieldValue]) => fieldValue !== undefined && fieldValue !== null && fieldValue !== ''),
  ) as Partial<T>;
}
