import { BellOutlined, CheckOutlined, ReloadOutlined, SearchOutlined } from '@ant-design/icons';
import { App, Button, Card, Form, Select, Space, Table, Tag, Typography } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useCallback, useEffect, useState } from 'react';
import {
  acknowledgeAlert,
  listInventoryAlerts,
  scanInventoryAlerts,
  type InventoryAlertRecord,
} from '@/services/inventory/alertService';

const DEFAULT_QUERY_FORM = { status: 'ACTIVE' };

export function InventoryAlertPage() {
  const { message } = App.useApp();
  const [form] = Form.useForm<typeof DEFAULT_QUERY_FORM>();
  const [alerts, setAlerts] = useState<InventoryAlertRecord[]>([]);
  const [loading, setLoading] = useState(false);
  const [scanning, setScanning] = useState(false);
  const [submittingId, setSubmittingId] = useState<string | null>(null);

  const columns: ColumnsType<InventoryAlertRecord> = [
    { title: '规则', dataIndex: 'ruleName', render: (value) => value || '-' },
    { title: '类型', dataIndex: 'alertTypeLabel', width: 120, render: (value) => value || '-' },
    { title: '库存对象', key: 'item', render: (_, record) => record.skuCode || record.materialName || '-' },
    { title: '仓库', dataIndex: 'warehouseName', width: 140, render: (value) => value || '-' },
    { title: '当前值', dataIndex: 'currentValue', width: 100, render: (value) => value ?? '-' },
    { title: '阈值', dataIndex: 'thresholdValue', width: 100, render: (value) => value ?? '-' },
    { title: '状态', dataIndex: 'statusLabel', width: 120, render: renderStatus },
    {
      title: '操作',
      key: 'action',
      width: 120,
      render: (_, record) => (
        <Button
          size="small"
          icon={<CheckOutlined />}
          aria-label={`确认预警 ${record.id}`}
          disabled={record.status !== 'ACTIVE'}
          loading={submittingId === record.id}
          onClick={() => handleAcknowledge(record.id)}
        >
          确认
        </Button>
      ),
    },
  ];

  const loadAlerts = useCallback(async (values = form.getFieldsValue()) => {
    setLoading(true);
    try {
      const records = await listInventoryAlerts({ status: values.status });
      setAlerts(records);
    } catch (error) {
      message.error(error instanceof Error ? error.message : '查询库存预警失败');
    } finally {
      setLoading(false);
    }
  }, [form, message]);

  useEffect(() => {
    void loadAlerts(DEFAULT_QUERY_FORM);
  }, [loadAlerts]);

  const handleScan = async () => {
    setScanning(true);
    try {
      const result = await scanInventoryAlerts();
      message.success(`扫描完成，新增 ${result.created ?? 0} 条预警`);
      await loadAlerts();
    } catch (error) {
      message.error(error instanceof Error ? error.message : '扫描库存预警失败');
    } finally {
      setScanning(false);
    }
  };

  const handleAcknowledge = async (alertId: string) => {
    setSubmittingId(alertId);
    try {
      await acknowledgeAlert(alertId);
      message.success('预警已确认');
      await loadAlerts();
    } catch (error) {
      message.error(error instanceof Error ? error.message : '确认预警失败');
    } finally {
      setSubmittingId(null);
    }
  };

  return (
    <Space direction="vertical" size={16} style={{ width: '100%' }}>
      <Card
        title="库存预警"
        extra={
          <Button icon={<ReloadOutlined />} loading={scanning} onClick={handleScan}>
            扫描库存预警
          </Button>
        }
      >
        <Form form={form} layout="inline" initialValues={DEFAULT_QUERY_FORM} onFinish={loadAlerts}>
          <Form.Item label="预警状态" name="status">
            <Select
              style={{ width: 160 }}
              options={[
                { label: '生效中', value: 'ACTIVE' },
                { label: '已确认', value: 'ACKNOWLEDGED' },
                { label: '已解决', value: 'RESOLVED' },
              ]}
            />
          </Form.Item>
          <Form.Item>
            <Button htmlType="submit" icon={<SearchOutlined />} loading={loading}>
              查询
            </Button>
          </Form.Item>
        </Form>
      </Card>

      <Card title="预警列表" extra={<BellOutlined />}>
        {alerts.length === 0 ? (
          <Typography.Text type="secondary">暂无库存预警记录。</Typography.Text>
        ) : (
          <Table rowKey="id" columns={columns} dataSource={alerts} loading={loading} pagination={false} />
        )}
      </Card>
    </Space>
  );
}

function renderStatus(value?: string | null) {
  return value ? <Tag color="warning">{value}</Tag> : '-';
}
