import { CheckOutlined, ReloadOutlined, SaveOutlined } from '@ant-design/icons';
import { App, Button, Card, Space, Switch, Table, Tag, Typography } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useCallback, useEffect, useState } from 'react';
import {
  listNotificationPreferences,
  markAllNotificationsRead,
  markNotificationRead,
  pageNotifications,
  updateNotificationPreference,
  type NotificationPreferenceRecord,
  type NotificationRecord,
} from '@/services/notification/notificationService';

export function NotificationCenterPage() {
  const { message } = App.useApp();
  const [notifications, setNotifications] = useState<NotificationRecord[]>([]);
  const [preferences, setPreferences] = useState<NotificationPreferenceRecord[]>([]);
  const [loading, setLoading] = useState(false);
  const [savingCategory, setSavingCategory] = useState<string | null>(null);

  const loadData = useCallback(async () => {
    setLoading(true);
    try {
      const [notificationPage, preferenceRows] = await Promise.all([
        pageNotifications({ pageNum: 1, pageSize: 20 }),
        listNotificationPreferences(),
      ]);
      setNotifications(notificationPage.records ?? []);
      setPreferences(preferenceRows);
    } catch (error) {
      message.error(error instanceof Error ? error.message : '查询通知中心失败');
    } finally {
      setLoading(false);
    }
  }, [message]);

  useEffect(() => {
    void loadData();
  }, [loadData]);

  const handleMarkRead = async (notificationId: string) => {
    setLoading(true);
    try {
      await markNotificationRead(notificationId);
      message.success('通知已标记为已读');
      await loadData();
    } catch (error) {
      message.error(error instanceof Error ? error.message : '标记已读失败');
    } finally {
      setLoading(false);
    }
  };

  const handleMarkAllRead = async () => {
    setLoading(true);
    try {
      await markAllNotificationsRead();
      message.success('所有通知已标记为已读');
      await loadData();
    } catch (error) {
      message.error(error instanceof Error ? error.message : '全部已读失败');
    } finally {
      setLoading(false);
    }
  };

  const updatePreferenceState = (category: string, key: keyof NotificationPreferenceRecord, value: boolean) => {
    setPreferences((rows) => rows.map((row) => (row.category === category ? { ...row, [key]: value } : row)));
  };

  const handleSavePreference = async (preference: NotificationPreferenceRecord) => {
    setSavingCategory(preference.category);
    try {
      await updateNotificationPreference({
        category: preference.category,
        channelSite: Boolean(preference.channelSite),
        channelWechat: Boolean(preference.channelWechat),
        channelDingtalk: Boolean(preference.channelDingtalk),
      });
      message.success('通知偏好已保存');
    } catch (error) {
      message.error(error instanceof Error ? error.message : '保存通知偏好失败');
    } finally {
      setSavingCategory(null);
    }
  };

  const notificationColumns: ColumnsType<NotificationRecord> = [
    { title: '标题', dataIndex: 'title', render: (value) => value || '-' },
    { title: '分类', dataIndex: 'categoryLabel', width: 120, render: (value) => value || '-' },
    { title: '业务单号', dataIndex: 'businessNo', width: 160, render: (value) => value || '-' },
    { title: '状态', dataIndex: 'isRead', width: 100, render: (value) => <Tag color={value ? 'default' : 'blue'}>{value ? '已读' : '未读'}</Tag> },
    { title: '时间', dataIndex: 'createdAt', width: 180, render: (value) => value || '-' },
    {
      title: '操作',
      key: 'action',
      width: 120,
      render: (_, record) => (
        <Button
          size="small"
          icon={<CheckOutlined />}
          disabled={Boolean(record.isRead)}
          aria-label={`标记已读 ${record.id}`}
          onClick={() => handleMarkRead(record.id)}
        >
          已读
        </Button>
      ),
    },
  ];

  const preferenceColumns: ColumnsType<NotificationPreferenceRecord> = [
    { title: '分类', dataIndex: 'categoryLabel', render: (value) => value || '-' },
    {
      title: '站内',
      dataIndex: 'channelSite',
      render: (value, record) => (
        <Switch checked={Boolean(value)} onChange={(checked) => updatePreferenceState(record.category, 'channelSite', checked)} />
      ),
    },
    {
      title: '企微',
      dataIndex: 'channelWechat',
      render: (value, record) => (
        <Switch checked={Boolean(value)} onChange={(checked) => updatePreferenceState(record.category, 'channelWechat', checked)} />
      ),
    },
    {
      title: '钉钉',
      dataIndex: 'channelDingtalk',
      render: (value, record) => (
        <Switch checked={Boolean(value)} onChange={(checked) => updatePreferenceState(record.category, 'channelDingtalk', checked)} />
      ),
    },
    {
      title: '操作',
      key: 'action',
      render: (_, record) => (
        <Button
          size="small"
          icon={<SaveOutlined />}
          loading={savingCategory === record.category}
          aria-label={`保存偏好 ${record.category}`}
          onClick={() => handleSavePreference(record)}
        >
          保存
        </Button>
      ),
    },
  ];

  return (
    <Space direction="vertical" size={16} style={{ width: '100%' }}>
      <Card
        title="我的通知"
        extra={
          <Space>
            <Button icon={<CheckOutlined />} loading={loading} onClick={handleMarkAllRead}>
              全部已读
            </Button>
            <Button icon={<ReloadOutlined />} loading={loading} onClick={loadData}>
              刷新
            </Button>
          </Space>
        }
      >
        {notifications.length === 0 ? (
          <Typography.Text type="secondary">暂无通知。</Typography.Text>
        ) : (
          <Table rowKey="id" columns={notificationColumns} dataSource={notifications} loading={loading} pagination={false} />
        )}
      </Card>
      <Card title="通知偏好">
        <Table rowKey="category" columns={preferenceColumns} dataSource={preferences} loading={loading} pagination={false} />
      </Card>
    </Space>
  );
}
