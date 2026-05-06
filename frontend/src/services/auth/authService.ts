import { apiClient, unwrapApiResponse } from '@/services/http/apiClient';

interface LoginRequest {
  username: string;
  password: string;
}

interface LoginResponse {
  token: string;
  userId: number;
  username: string;
  realName: string;
  roleIds: number[];
  permissions: string[];
  menuTree: unknown[];
  passwordExpired: boolean;
}

export async function login(payload: LoginRequest): Promise<LoginResponse> {
  const response = await apiClient.post('/auth/login', payload);
  return unwrapApiResponse<LoginResponse>(response.data);
}
