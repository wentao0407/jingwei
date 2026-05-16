import { apiClient, unwrapApiResponse } from '@/services/http/apiClient';
import type { PageResult } from '@/services/master/customerService';
import { normalizeOptionalFields } from '@/services/shared/normalize';

export interface AuditLogQueryParams {
  current: number;
  size: number;
  userId?: string;
  module?: string;
  operationType?: string;
  startTime?: string;
  endTime?: string;
  keyword?: string;
}

export interface AuditLogRecord {
  id: string;
  userId?: string | null;
  username?: string | null;
  operationType?: string | null;
  module?: string | null;
  description?: string | null;
  oldValue?: string | null;
  newValue?: string | null;
  ipAddress?: string | null;
  createdAt?: string | null;
}

export interface DataScopeRecord {
  id?: string | null;
  roleId?: string | null;
  scopeType?: string | null;
  scopeValue?: string | null;
}

export interface DataScopePayload {
  scopeType: string;
  scopeValue: string;
}

export interface ConfigureDataScopePayload {
  scopes: DataScopePayload[];
}

export async function listAuditLogs(params: AuditLogQueryParams): Promise<PageResult<AuditLogRecord>> {
  const response = await apiClient.post('/system/audit-log/page', normalizeAuditLogQuery(params));
  return unwrapApiResponse<PageResult<AuditLogRecord>>(response.data);
}

export async function queryDataScope(roleId: string): Promise<DataScopeRecord[]> {
  const response = await apiClient.post('/system/data-scope/query', null, {
    params: { roleId: roleId.trim() },
  });
  return unwrapApiResponse<DataScopeRecord[]>(response.data);
}

export async function configureDataScope(roleId: string, payload: ConfigureDataScopePayload): Promise<void> {
  const response = await apiClient.post('/system/data-scope/configure', normalizeDataScopePayload(payload), {
    params: { roleId: roleId.trim() },
  });
  return unwrapApiResponse<void>(response.data);
}

function normalizeAuditLogQuery(params: AuditLogQueryParams): Partial<AuditLogQueryParams> {
  return normalizeOptionalFields({
    ...params,
    current: Math.max(1, params.current),
    size: Math.max(1, params.size),
  });
}

function normalizeDataScopePayload(payload: ConfigureDataScopePayload): ConfigureDataScopePayload {
  return {
    scopes: payload.scopes
      .map((scope) => normalizeOptionalFields(scope))
      .filter((scope): scope is DataScopePayload => Boolean(scope.scopeType && scope.scopeValue)),
  };
}
