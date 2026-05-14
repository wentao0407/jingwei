import { beforeEach, describe, expect, it, vi } from 'vitest';
import {
  listNotificationPreferences,
  markAllNotificationsRead,
  markNotificationRead,
  pageNotifications,
  updateNotificationPreference,
} from './notificationService';
import { apiClient } from '@/services/http/apiClient';

vi.mock('@/services/http/apiClient', async () => {
  const actual = await vi.importActual<typeof import('@/services/http/apiClient')>('@/services/http/apiClient');
  return {
    ...actual,
    apiClient: {
      post: vi.fn(),
    },
  };
});

const mockedPost = vi.mocked(apiClient.post);

describe('notificationService', () => {
  beforeEach(() => {
    mockedPost.mockReset();
  });

  it('queries notifications and updates read state and preferences', async () => {
    mockedPost
      .mockResolvedValueOnce({ data: ok({ records: [], total: 0 }) })
      .mockResolvedValueOnce({ data: ok(null) })
      .mockResolvedValueOnce({ data: ok(null) })
      .mockResolvedValueOnce({ data: ok([]) })
      .mockResolvedValueOnce({ data: ok(null) });

    await pageNotifications({ pageNum: 0, pageSize: 0, category: ' ORDER ', isRead: undefined });
    await markNotificationRead('70001');
    await markAllNotificationsRead();
    await listNotificationPreferences();
    await updateNotificationPreference({
      category: ' APPROVAL ',
      channelSite: true,
      channelWechat: false,
      channelDingtalk: true,
    });

    expect(mockedPost).toHaveBeenNthCalledWith(1, '/notification/page', {
      pageNum: 1,
      pageSize: 1,
      category: 'ORDER',
    });
    expect(mockedPost).toHaveBeenNthCalledWith(2, '/notification/mark-read', { notificationId: '70001' });
    expect(mockedPost).toHaveBeenNthCalledWith(3, '/notification/mark-all-read');
    expect(mockedPost).toHaveBeenNthCalledWith(4, '/notification/preference/detail');
    expect(mockedPost).toHaveBeenNthCalledWith(5, '/notification/preference/update', {
      category: 'APPROVAL',
      channelSite: true,
      channelWechat: false,
      channelDingtalk: true,
    });
  });
});

function ok(data: unknown) {
  return { code: 0, message: 'success', success: true, data };
}
