import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { App as AntdApp } from 'antd';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { NotificationCenterPage } from './NotificationCenterPage';
import {
  listNotificationPreferences,
  markAllNotificationsRead,
  markNotificationRead,
  pageNotifications,
  updateNotificationPreference,
} from '@/services/notification/notificationService';

vi.mock('@/services/notification/notificationService', () => ({
  listNotificationPreferences: vi.fn(),
  markAllNotificationsRead: vi.fn(),
  markNotificationRead: vi.fn(),
  pageNotifications: vi.fn(),
  updateNotificationPreference: vi.fn(),
}));

const mockedListPreferences = vi.mocked(listNotificationPreferences);
const mockedMarkAllRead = vi.mocked(markAllNotificationsRead);
const mockedMarkRead = vi.mocked(markNotificationRead);
const mockedPageNotifications = vi.mocked(pageNotifications);
const mockedUpdatePreference = vi.mocked(updateNotificationPreference);

describe('NotificationCenterPage', () => {
  beforeEach(() => {
    mockedListPreferences.mockReset();
    mockedListPreferences.mockResolvedValue([
      {
        id: '1',
        category: 'APPROVAL',
        categoryLabel: '审批通知',
        channelSite: true,
        channelWechat: true,
        channelDingtalk: false,
      },
    ]);
    mockedMarkAllRead.mockReset();
    mockedMarkAllRead.mockResolvedValue(undefined);
    mockedMarkRead.mockReset();
    mockedMarkRead.mockResolvedValue(undefined);
    mockedPageNotifications.mockReset();
    mockedPageNotifications.mockResolvedValue({
      current: 1,
      size: 20,
      total: 1,
      pages: 1,
      records: [
        {
          id: '70001',
          title: '销售订单审批通过',
          content: 'SO-20260501 已通过审批',
          categoryLabel: '订单通知',
          businessNo: 'SO-20260501',
          isRead: false,
          createdAt: '2026-05-14T10:00:00',
        },
      ],
    });
    mockedUpdatePreference.mockReset();
    mockedUpdatePreference.mockResolvedValue(undefined);
  });

  it('loads notifications and marks messages as read', async () => {
    renderPage();

    expect(await screen.findByText('销售订单审批通过')).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: '标记已读 70001' }));

    await waitFor(() => expect(mockedMarkRead).toHaveBeenCalledWith('70001'));

    fireEvent.click(screen.getByRole('button', { name: /全部已读/ }));
    await waitFor(() => expect(mockedMarkAllRead).toHaveBeenCalled());
  });

  it('updates notification preferences', async () => {
    renderPage();

    fireEvent.click(await screen.findByRole('button', { name: '保存偏好 APPROVAL' }));

    await waitFor(() =>
      expect(mockedUpdatePreference).toHaveBeenCalledWith({
        category: 'APPROVAL',
        channelSite: true,
        channelWechat: true,
        channelDingtalk: false,
      }),
    );
  });
});

function renderPage() {
  render(
    <AntdApp>
      <NotificationCenterPage />
    </AntdApp>,
  );
}
