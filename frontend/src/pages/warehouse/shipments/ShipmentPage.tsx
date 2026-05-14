import { SendOutlined } from '@ant-design/icons';
import { App, Button, Card, Form, Input, Space, Typography } from 'antd';
import { useState } from 'react';
import { confirmShipment } from '@/services/warehouse/shipmentService';

const DEFAULT_FORM = { outboundId: '', salesOrderId: '' };

export function ShipmentPage() {
  const { message } = App.useApp();
  const [form] = Form.useForm<typeof DEFAULT_FORM>();
  const [submitting, setSubmitting] = useState(false);

  const handleConfirmShipment = async () => {
    const values = await form.validateFields().catch(() => null);
    if (!values) return;

    setSubmitting(true);
    try {
      await confirmShipment({
        outboundId: values.outboundId.trim(),
        salesOrderId: values.salesOrderId?.trim() || undefined,
      });
      message.success('发运已确认');
      form.resetFields();
    } catch (error) {
      message.error(error instanceof Error ? error.message : '确认发运失败');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Space direction="vertical" size={16} style={{ width: '100%' }}>
      <Card title="发运确认" extra={<SendOutlined />}>
        <Form form={form} layout="inline" initialValues={DEFAULT_FORM}>
          <Form.Item label="出库单 ID" name="outboundId" rules={[{ required: true, message: '请输入出库单 ID' }]}>
            <Input placeholder="出库单 ID" allowClear />
          </Form.Item>
          <Form.Item label="销售订单 ID" name="salesOrderId">
            <Input placeholder="可选" allowClear />
          </Form.Item>
          <Form.Item>
            <Button type="primary" icon={<SendOutlined />} loading={submitting} onClick={handleConfirmShipment}>
              确认发运
            </Button>
          </Form.Item>
        </Form>
      </Card>
      <Card>
        <Typography.Text type="secondary">
          当前后端仅开放发运确认接口，页面保留为单据操作台，避免展示不存在的发运列表数据。
        </Typography.Text>
      </Card>
    </Space>
  );
}
