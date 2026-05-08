import { apiClient, unwrapApiResponse } from '@/services/http/apiClient';
import type { PageResult } from './userService';

export interface RoleQueryParams {
  current: number;
  size: number;
  keyword?: string;
  status?: string;
}

export interface CreateRolePayload {
  roleCode: string;
  roleName: string;
  description?: string;
}

export interface UpdateRolePayload {
  roleName?: string;
  description?: string;
  status?: string;
}

export interface RoleRecord {
  id: string;
  roleCode: string;
  roleName: string;
  description?: string | null;
  status: string;
  createdAt?: string | null;
  updatedAt?: string | null;
}

export async function listRoles(params: RoleQueryParams): Promise<PageResult<RoleRecord>> {
  const response = await apiClient.post('/system/role/page', normalizeRoleQuery(params));
  return unwrapApiResponse<PageResult<RoleRecord>>(response.data);
}

export async function createRole(payload: CreateRolePayload): Promise<RoleRecord> {
  const response = await apiClient.post('/system/role/create', normalizeOptionalFields(payload));
  return unwrapApiResponse<RoleRecord>(response.data);
}

export async function updateRole(roleId: string, payload: UpdateRolePayload): Promise<RoleRecord> {
  const response = await apiClient.post('/system/role/update', normalizeOptionalFields(payload), {
    params: { roleId },
  });
  return unwrapApiResponse<RoleRecord>(response.data);
}

function normalizeRoleQuery(params: RoleQueryParams): RoleQueryParams {
  const keyword = params.keyword?.trim();

  return {
    current: Math.max(1, params.current),
    size: Math.max(1, params.size),
    ...(keyword ? { keyword } : {}),
    ...(params.status ? { status: params.status } : {}),
  };
}

function normalizeOptionalFields<T extends object>(payload: T): T {
  return Object.fromEntries(
    Object.entries(payload)
      .map(([key, value]) => [key, typeof value === 'string' ? value.trim() : value])
      .filter(([, value]) => value !== undefined && value !== null && value !== ''),
  ) as T;
}
