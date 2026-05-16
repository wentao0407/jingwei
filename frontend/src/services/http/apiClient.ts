import axios, { AxiosError } from 'axios';
import { emitUnauthorized } from '@/shared/auth/authEvents';
import { clearAuthSession } from '@/shared/storage/authSessionStorage';
import { clearAccessToken, getAccessToken } from '@/shared/storage/tokenStorage';

const UNAUTHORIZED_CODE = 10005;
const DEFAULT_ERROR_MESSAGE = '请求失败，请稍后重试';

export interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
  success: boolean;
}

export const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 15000,
});

apiClient.interceptors.request.use((config) => {
  const token = getAccessToken();

  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }

  return config;
});

apiClient.interceptors.response.use(
  (response) => response,
  (error: AxiosError<ApiResponse<unknown>>) => {
    if (error.response?.status === 401 || error.response?.data?.code === UNAUTHORIZED_CODE) {
      clearAccessToken();
      clearAuthSession();
      emitUnauthorized();
    }

    return Promise.reject(error);
  },
);

export function unwrapApiResponse<T>(response: ApiResponse<T>): T {
  if (!response.success) {
    throw new Error(response.message || DEFAULT_ERROR_MESSAGE);
  }

  return response.data;
}

export function getApiErrorMessage(error: unknown): string {
  const responseMessage = getResponseMessage(error);

  if (responseMessage) {
    return responseMessage;
  }

  if (axios.isAxiosError<ApiResponse<unknown>>(error)) {
    return error.message || DEFAULT_ERROR_MESSAGE;
  }

  if (error instanceof Error) {
    return error.message || DEFAULT_ERROR_MESSAGE;
  }

  return DEFAULT_ERROR_MESSAGE;
}

function getResponseMessage(error: unknown): string | null {
  if (!isRecord(error)) {
    return null;
  }

  const response = error.response;

  if (!isRecord(response)) {
    return null;
  }

  const data = response.data;

  if (!isRecord(data) || typeof data.message !== 'string') {
    return null;
  }

  return data.message;
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null;
}
