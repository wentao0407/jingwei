import { apiClient, unwrapApiResponse } from '@/services/http/apiClient';
import type { PageResult } from '@/services/master/customerService';
import { normalizeOptionalFields } from '@/services/shared/normalize';

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

export interface WaveQueryParams {
  current: number;
  size: number;
  warehouseId?: string;
  status?: string;
  waveNo?: string;
}

export interface PickItemRecord {
  id: string;
  skuId?: string | null;
  materialId?: string | null;
  batchNo?: string | null;
  plannedQty?: number | null;
  actualQty?: number | null;
  status?: string | null;
}

export interface PickListRecord {
  id: string;
  pickListNo?: string | null;
  status?: string | null;
  items?: PickItemRecord[] | null;
}

export interface WaveRecord {
  id: string;
  waveNo?: string | null;
  warehouseId?: string | null;
  strategy?: string | null;
  status?: string | null;
  remark?: string | null;
  createdAt?: string | null;
  pickLists?: PickListRecord[] | null;
}

export async function pageWaves(params: WaveQueryParams): Promise<PageResult<WaveRecord>> {
  const response = await apiClient.post('/warehouse/wave/page', normalizePageQuery(params));
  return unwrapApiResponse<PageResult<WaveRecord>>(response.data);
}

export async function getWaveDetail(waveId: string): Promise<WaveRecord> {
  const response = await apiClient.post('/warehouse/wave/detail', null, { params: { waveId: waveId.trim() } });
  return unwrapApiResponse<WaveRecord>(response.data);
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

function normalizePageQuery<T extends { current: number; size: number }>(params: T): Partial<T> {
  return normalizeOptionalFields({ ...params, current: Math.max(1, params.current), size: Math.max(1, params.size) });
}
