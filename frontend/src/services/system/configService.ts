import { apiClient, unwrapApiResponse } from '@/services/http/apiClient';

export interface SystemConfigRecord {
  id: string;
  configKey: string;
  configValue: string;
  configGroup?: string | null;
  description?: string | null;
  needRestart?: boolean | null;
  remark?: string | null;
  createdAt?: string | null;
  updatedAt?: string | null;
}

export interface UpdateSystemConfigPayload {
  configValue?: string;
  description?: string;
  needRestart?: boolean;
  remark: string;
}

export interface CreateSystemConfigPayload {
  configKey: string;
  configValue: string;
  configGroup?: string;
  description?: string;
  needRestart?: boolean;
}

export async function listSystemConfigs(): Promise<SystemConfigRecord[]> {
  const response = await apiClient.post('/system/config/list');
  return unwrapApiResponse<SystemConfigRecord[]>(response.data);
}

export async function createSystemConfig(payload: CreateSystemConfigPayload): Promise<SystemConfigRecord> {
  const response = await apiClient.post('/system/config/create', normalizeOptionalFields(payload));
  return unwrapApiResponse<SystemConfigRecord>(response.data);
}

export async function updateSystemConfig(
  configId: string,
  payload: UpdateSystemConfigPayload,
): Promise<SystemConfigRecord> {
  const response = await apiClient.post('/system/config/update', normalizeOptionalFields(payload), {
    params: { configId },
  });
  return unwrapApiResponse<SystemConfigRecord>(response.data);
}

function normalizeOptionalFields<T extends object>(payload: T): T {
  return Object.fromEntries(
    Object.entries(payload)
      .map(([key, value]) => [key, typeof value === 'string' ? value.trim() : value])
      .filter(([, value]) => value !== undefined && value !== null && value !== ''),
  ) as T;
}
