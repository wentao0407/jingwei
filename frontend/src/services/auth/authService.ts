import { apiClient, unwrapApiResponse } from '@/services/http/apiClient';
import type { AuthMenuItem } from '@/shared/storage/authSessionStorage';

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  userId: string;
  username: string;
  realName: string;
  roleIds: string[];
  permissions: string[];
  menuTree: AuthMenuItem[];
  passwordExpired: boolean;
}

export interface UserPermissionResponse {
  menuTree: AuthMenuItem[];
  permissions: string[];
}

export async function login(payload: LoginRequest): Promise<LoginResponse> {
  const response = await apiClient.post('/auth/login', payload);
  return unwrapApiResponse<LoginResponse>(response.data);
}

export async function getCurrentUserPermissions(): Promise<UserPermissionResponse> {
  const response = await apiClient.post('/system/menu/permissions');
  return unwrapApiResponse<UserPermissionResponse>(response.data);
}
