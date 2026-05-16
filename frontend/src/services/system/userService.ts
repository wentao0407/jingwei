import { apiClient, unwrapApiResponse } from '@/services/http/apiClient';
import { normalizeOptionalFields } from '@/services/shared/normalize';

export interface UserQueryParams {
  current: number;
  size: number;
  keyword?: string;
  status?: string;
}

export interface CreateUserPayload {
  username: string;
  password: string;
  realName?: string;
  phone?: string;
  email?: string;
}

export interface UpdateUserPayload {
  realName?: string;
  phone?: string;
  email?: string;
  status?: string;
}

export interface AssignUserRolesPayload {
  roleIds: string[];
}

export interface ChangeUserPasswordPayload {
  oldPassword: string;
  newPassword: string;
}

export interface UserRecord {
  id: string;
  username: string;
  realName?: string | null;
  phone?: string | null;
  email?: string | null;
  status: string;
  roleIds: string[];
  createdAt?: string | null;
  updatedAt?: string | null;
}

export interface PageResult<T> {
  records: T[];
  total: number;
  size: number;
  current: number;
  pages: number;
}

export async function listUsers(params: UserQueryParams): Promise<PageResult<UserRecord>> {
  const response = await apiClient.post('/system/user/page', normalizeUserQuery(params));
  return unwrapApiResponse<PageResult<UserRecord>>(response.data);
}

export async function createUser(payload: CreateUserPayload): Promise<UserRecord> {
  const response = await apiClient.post('/system/user/create', normalizeOptionalFields(payload));
  return unwrapApiResponse<UserRecord>(response.data);
}

export async function updateUser(userId: string, payload: UpdateUserPayload): Promise<UserRecord> {
  const response = await apiClient.post('/system/user/update', normalizeOptionalFields(payload), {
    params: { userId: userId.trim() },
  });
  return unwrapApiResponse<UserRecord>(response.data);
}

export async function getUserDetail(userId: string): Promise<UserRecord> {
  const response = await apiClient.post('/system/user/detail', null, {
    params: { userId: userId.trim() },
  });
  return unwrapApiResponse<UserRecord>(response.data);
}

export async function changeUserPassword(userId: string, payload: ChangeUserPasswordPayload): Promise<void> {
  const response = await apiClient.post('/system/user/changePassword', normalizeOptionalFields(payload), {
    params: { userId: userId.trim() },
  });
  return unwrapApiResponse<void>(response.data);
}

export async function deactivateUser(userId: string): Promise<void> {
  const response = await apiClient.post('/system/user/deactivate', null, {
    params: { userId: userId.trim() },
  });
  return unwrapApiResponse<void>(response.data);
}

export async function assignUserRoles(userId: string, payload: AssignUserRolesPayload): Promise<void> {
  const response = await apiClient.post('/system/user/assignRoles', payload, {
    params: { userId: userId.trim() },
  });
  return unwrapApiResponse<void>(response.data);
}

function normalizeUserQuery(params: UserQueryParams): UserQueryParams {
  const keyword = params.keyword?.trim();

  return {
    current: Math.max(1, params.current),
    size: Math.max(1, params.size),
    ...(keyword ? { keyword } : {}),
    ...(params.status ? { status: params.status } : {}),
  };
}
