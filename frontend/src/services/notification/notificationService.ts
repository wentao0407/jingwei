import { apiClient, unwrapApiResponse } from '@/services/http/apiClient';
import type { PageResult } from '@/services/master/customerService';

export interface NotificationRecord {
  id: string;
  title?: string | null;
  content?: string | null;
  category?: string | null;
  categoryLabel?: string | null;
  businessType?: string | null;
  businessId?: string | null;
  businessNo?: string | null;
  senderId?: string | null;
  isRead?: boolean | null;
  readAt?: string | null;
  createdAt?: string | null;
}

export interface NotificationPreferenceRecord {
  id?: string | null;
  category: string;
  categoryLabel?: string | null;
  channelSite?: boolean | null;
  channelWechat?: boolean | null;
  channelDingtalk?: boolean | null;
}

export interface NotificationQueryParams {
  pageNum: number;
  pageSize: number;
  category?: string;
  isRead?: boolean;
}

export interface UpdateNotificationPreferencePayload {
  category: string;
  channelSite?: boolean;
  channelWechat?: boolean;
  channelDingtalk?: boolean;
}

export async function pageNotifications(
  params: NotificationQueryParams,
): Promise<PageResult<NotificationRecord>> {
  const response = await apiClient.post('/notification/page', normalizePageQuery(params));
  return unwrapApiResponse<PageResult<NotificationRecord>>(response.data);
}

export async function markNotificationRead(notificationId: string): Promise<void> {
  const response = await apiClient.post('/notification/mark-read', { notificationId: notificationId.trim() });
  return unwrapApiResponse<void>(response.data);
}

export async function markAllNotificationsRead(): Promise<void> {
  const response = await apiClient.post('/notification/mark-all-read');
  return unwrapApiResponse<void>(response.data);
}

export async function listNotificationPreferences(): Promise<NotificationPreferenceRecord[]> {
  const response = await apiClient.post('/notification/preference/detail');
  return unwrapApiResponse<NotificationPreferenceRecord[]>(response.data);
}

export async function updateNotificationPreference(
  payload: UpdateNotificationPreferencePayload,
): Promise<void> {
  const response = await apiClient.post('/notification/preference/update', normalizeOptionalFields(payload));
  return unwrapApiResponse<void>(response.data);
}

function normalizePageQuery(params: NotificationQueryParams): Partial<NotificationQueryParams> {
  return normalizeOptionalFields({
    ...params,
    pageNum: Math.max(1, params.pageNum),
    pageSize: Math.max(1, params.pageSize),
  });
}

function normalizeOptionalFields<T extends object>(value: T): Partial<T> {
  return Object.fromEntries(
    Object.entries(value)
      .map(([key, fieldValue]) => [key, typeof fieldValue === 'string' ? fieldValue.trim() : fieldValue])
      .filter(([, fieldValue]) => fieldValue !== undefined && fieldValue !== null && fieldValue !== ''),
  ) as Partial<T>;
}
