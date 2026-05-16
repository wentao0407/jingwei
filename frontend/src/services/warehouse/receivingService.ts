import { apiClient, unwrapApiResponse } from '@/services/http/apiClient';
import { normalizeOptionalFields } from '@/services/shared/normalize';

export interface ReceivingLineRecord {
  id: string;
  asnLineId?: string | null;
  materialId?: string | null;
  materialName?: string | null;
  expectedQty?: number | null;
  receivedQty?: number | null;
  rollCount?: number | null;
  differenceQty?: number | null;
  differenceReason?: string | null;
  batchNo?: string | null;
  qcStatus?: string | null;
  qcStatusLabel?: string | null;
  putawayStatus?: string | null;
  putawayStatusLabel?: string | null;
  putawayLocationId?: string | null;
  putawayLocationCode?: string | null;
  remark?: string | null;
}

export interface ReceivingOrderRecord {
  id: string;
  receivingNo?: string | null;
  asnId?: string | null;
  asnNo?: string | null;
  warehouseId?: string | null;
  warehouseName?: string | null;
  receivingDate?: string | null;
  status?: string | null;
  statusLabel?: string | null;
  receiverId?: string | null;
  dockNo?: string | null;
  remark?: string | null;
  createdAt?: string | null;
  lines?: ReceivingLineRecord[] | null;
}

export interface CreateReceivingPayload {
  asnId: string;
  warehouseId: string;
  dockNo?: string;
}

export interface ConfirmReceivePayload {
  receivingLineId: string;
  receivedQty: number;
  rollCount?: number;
}

export interface PutawayLocationRecord {
  locationId: string;
  fullCode: string;
  locationType?: string | null;
  capacity?: number | null;
  usedCapacity?: number | null;
  remainingCapacity?: number | null;
}

export interface ConfirmPutawayPayload {
  receivingLineId: string;
  locationId: string;
}

export async function createReceivingFromAsn(payload: CreateReceivingPayload): Promise<ReceivingOrderRecord> {
  const response = await apiClient.post('/warehouse/receiving/create', normalizeOptionalFields(payload));
  return unwrapApiResponse<ReceivingOrderRecord>(response.data);
}

export async function confirmReceive(payload: ConfirmReceivePayload): Promise<void> {
  const response = await apiClient.post('/warehouse/receiving/confirm', normalizeOptionalFields(payload));
  return unwrapApiResponse<void>(response.data);
}

export async function getReceivingDetail(receivingId: string): Promise<ReceivingOrderRecord> {
  const response = await apiClient.post('/warehouse/receiving/detail', null, { params: { receivingId } });
  return unwrapApiResponse<ReceivingOrderRecord>(response.data);
}

export async function suggestReceivingLocations(receivingLineId: string): Promise<PutawayLocationRecord[]> {
  const response = await apiClient.post('/warehouse/receiving/suggest-locations', null, {
    params: { receivingLineId },
  });
  return unwrapApiResponse<PutawayLocationRecord[]>(response.data);
}

export async function confirmPutaway(payload: ConfirmPutawayPayload): Promise<void> {
  const response = await apiClient.post('/warehouse/receiving/putaway', normalizeOptionalFields(payload));
  return unwrapApiResponse<void>(response.data);
}

