import { apiClient, unwrapApiResponse } from '@/services/http/apiClient';

export interface SeasonQueryParams {
  year?: number;
  seasonType?: string;
  status?: string;
}

export interface WaveRecord {
  id: string;
  seasonId: string;
  code: string;
  name: string;
  deliveryDate?: string | null;
  sortOrder?: number | null;
  createdAt?: string | null;
  updatedAt?: string | null;
}

export interface SeasonRecord {
  id: string;
  code: string;
  name: string;
  year: number;
  seasonType: string;
  startDate: string;
  endDate: string;
  status: string;
  waves?: WaveRecord[] | null;
  createdAt?: string | null;
  updatedAt?: string | null;
}

export interface CreateSeasonPayload {
  code: string;
  name: string;
  year: number;
  seasonType: string;
  startDate: string;
  endDate: string;
}

export interface UpdateSeasonPayload {
  name?: string;
  startDate?: string;
  endDate?: string;
}

export interface CreateWavePayload {
  code: string;
  name: string;
  deliveryDate?: string;
  sortOrder?: number;
}

export interface UpdateWavePayload {
  name?: string;
  deliveryDate?: string;
  sortOrder?: number;
}

export async function listSeasons(params: SeasonQueryParams = {}): Promise<SeasonRecord[]> {
  const response = await apiClient.post('/master/season/list', null, {
    params: normalizeOptionalFields(params),
  });
  return unwrapApiResponse<SeasonRecord[]>(response.data);
}

export async function getSeasonDetail(seasonId: string): Promise<SeasonRecord> {
  const response = await apiClient.post('/master/season/detail', null, {
    params: { seasonId },
  });
  return unwrapApiResponse<SeasonRecord>(response.data);
}

export async function createSeason(payload: CreateSeasonPayload): Promise<SeasonRecord> {
  const response = await apiClient.post('/master/season/create', normalizeOptionalFields(payload));
  return unwrapApiResponse<SeasonRecord>(response.data);
}

export async function updateSeason(seasonId: string, payload: UpdateSeasonPayload): Promise<SeasonRecord> {
  const response = await apiClient.post('/master/season/update', normalizeOptionalFields(payload), {
    params: { seasonId },
  });
  return unwrapApiResponse<SeasonRecord>(response.data);
}

export async function closeSeason(seasonId: string): Promise<void> {
  const response = await apiClient.post('/master/season/close', null, {
    params: { seasonId },
  });
  return unwrapApiResponse<void>(response.data);
}

export async function deleteSeason(seasonId: string): Promise<void> {
  const response = await apiClient.post('/master/season/delete', null, {
    params: { seasonId },
  });
  return unwrapApiResponse<void>(response.data);
}

export async function createWave(seasonId: string, payload: CreateWavePayload): Promise<WaveRecord> {
  const response = await apiClient.post('/master/season/wave/create', normalizeOptionalFields(payload), {
    params: { seasonId },
  });
  return unwrapApiResponse<WaveRecord>(response.data);
}

export async function updateWave(waveId: string, payload: UpdateWavePayload): Promise<WaveRecord> {
  const response = await apiClient.post('/master/season/wave/update', normalizeOptionalFields(payload), {
    params: { waveId },
  });
  return unwrapApiResponse<WaveRecord>(response.data);
}

export async function deleteWave(waveId: string): Promise<void> {
  const response = await apiClient.post('/master/season/wave/delete', null, {
    params: { waveId },
  });
  return unwrapApiResponse<void>(response.data);
}

function normalizeOptionalFields<T extends object>(payload: T): Partial<T> {
  return Object.fromEntries(
    Object.entries(payload)
      .map(([key, value]) => [key, typeof value === 'string' ? value.trim() : value])
      .filter(([, value]) => value !== undefined && value !== null && value !== ''),
  ) as Partial<T>;
}
