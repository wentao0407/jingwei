import { AppstoreOutlined, CheckOutlined, CloseOutlined, InboxOutlined } from '@ant-design/icons';
import { App, Button, Card, Form, Input, Select, Space, Typography } from 'antd';
import { useState } from 'react';
import { cancelWave, completePickList, confirmPick, createWave } from '@/services/warehouse/waveService';

interface WaveFormValues {
  warehouseId: string;
  strategy: string;
  outboundOrderIds: string;
  remark?: string;
}

const DEFAULT_WAVE_FORM: WaveFormValues = {
  warehouseId: '',
  strategy: 'BY_CUSTOMER',
  outboundOrderIds: '',
};

export function WavePickingPage() {
  const { message } = App.useApp();
  const [waveForm] = Form.useForm<WaveFormValues>();
  const [pickForm] = Form.useForm<{ pickItemId: string; actualQty: string }>();
  const [completeForm] = Form.useForm<{ pickListId: string }>();
  const [cancelForm] = Form.useForm<{ waveId: string }>();
  const [createdWaveId, setCreatedWaveId] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const handleCreateWave = async () => {
    const values = await waveForm.validateFields().catch(() => null);
    if (!values) return;

    setSubmitting(true);
    try {
      const waveId = await createWave({
        warehouseId: values.warehouseId.trim(),
        strategy: values.strategy,
        outboundOrderIds: splitIds(values.outboundOrderIds),
        remark: values.remark?.trim() || undefined,
      });
      setCreatedWaveId(String(waveId));
      message.success('波次已创建');
    } catch (error) {
      message.error(error instanceof Error ? error.message : '创建波次失败');
    } finally {
      setSubmitting(false);
    }
  };

  const handleConfirmPick = async () => {
    const values = await pickForm.validateFields().catch(() => null);
    if (!values) return;

    setSubmitting(true);
    try {
      await confirmPick({ pickItemId: values.pickItemId.trim(), actualQty: Number(values.actualQty) });
      message.success('拣货已确认');
      pickForm.resetFields();
    } catch (error) {
      message.error(error instanceof Error ? error.message : '确认拣货失败');
    } finally {
      setSubmitting(false);
    }
  };

  const handleCompletePickList = async () => {
    const values = await completeForm.validateFields().catch(() => null);
    if (!values) return;

    setSubmitting(true);
    try {
      await completePickList(values.pickListId.trim());
      message.success('拣货单已完成');
      completeForm.resetFields();
    } catch (error) {
      message.error(error instanceof Error ? error.message : '完成拣货单失败');
    } finally {
      setSubmitting(false);
    }
  };

  const handleCancelWave = async () => {
    const values = await cancelForm.validateFields().catch(() => null);
    if (!values) return;

    setSubmitting(true);
    try {
      await cancelWave(values.waveId.trim());
      message.success('波次已取消');
      cancelForm.resetFields();
    } catch (error) {
      message.error(error instanceof Error ? error.message : '取消波次失败');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Space direction="vertical" size={16} style={{ width: '100%' }}>
      <Card title="创建波次" extra={<AppstoreOutlined />}>
        <Form form={waveForm} layout="inline" initialValues={DEFAULT_WAVE_FORM}>
          <Form.Item label="仓库 ID" name="warehouseId" rules={[{ required: true, message: '请输入仓库 ID' }]}>
            <Input placeholder="仓库 ID" allowClear />
          </Form.Item>
          <Form.Item label="策略" name="strategy" rules={[{ required: true, message: '请选择策略' }]}>
            <Select
              style={{ width: 160 }}
              options={[
                { label: '按客户', value: 'BY_CUSTOMER' },
                { label: '按承运商', value: 'BY_CARRIER' },
                { label: '按区域', value: 'BY_ZONE' },
              ]}
            />
          </Form.Item>
          <Form.Item
            label="出库单 ID"
            name="outboundOrderIds"
            rules={[{ required: true, message: '请输入出库单 ID' }]}
          >
            <Input placeholder="多个 ID 用逗号分隔" allowClear />
          </Form.Item>
          <Form.Item label="备注" name="remark">
            <Input placeholder="可选" allowClear />
          </Form.Item>
          <Form.Item>
            <Button type="primary" icon={<AppstoreOutlined />} loading={submitting} onClick={handleCreateWave}>
              创建波次
            </Button>
          </Form.Item>
        </Form>
        {createdWaveId ? (
          <Typography.Text type="secondary" style={{ display: 'block', marginTop: 12 }}>
            新波次 ID：{createdWaveId}
          </Typography.Text>
        ) : null}
      </Card>

      <Card title="拣货作业" extra={<InboxOutlined />}>
        <Space direction="vertical" size={12} style={{ width: '100%' }}>
          <Form form={pickForm} layout="inline">
            <Form.Item label="拣货项 ID" name="pickItemId" rules={[{ required: true, message: '请输入拣货项 ID' }]}>
              <Input placeholder="拣货项 ID" allowClear />
            </Form.Item>
            <Form.Item
              label="实拣数量"
              name="actualQty"
              rules={[{ required: true, message: '请输入实拣数量' }, { pattern: /^\d+(\.\d+)?$/, message: '请输入有效数量' }]}
            >
              <Input placeholder="实拣数量" inputMode="decimal" allowClear />
            </Form.Item>
            <Form.Item>
              <Button icon={<CheckOutlined />} loading={submitting} onClick={handleConfirmPick}>
                确认拣货
              </Button>
            </Form.Item>
          </Form>

          <Form form={completeForm} layout="inline">
            <Form.Item label="拣货单 ID" name="pickListId" rules={[{ required: true, message: '请输入拣货单 ID' }]}>
              <Input placeholder="拣货单 ID" allowClear />
            </Form.Item>
            <Form.Item>
              <Button icon={<CheckOutlined />} loading={submitting} onClick={handleCompletePickList}>
                完成拣货单
              </Button>
            </Form.Item>
          </Form>

          <Form form={cancelForm} layout="inline">
            <Form.Item label="波次 ID" name="waveId" rules={[{ required: true, message: '请输入波次 ID' }]}>
              <Input placeholder="波次 ID" allowClear />
            </Form.Item>
            <Form.Item>
              <Button danger icon={<CloseOutlined />} loading={submitting} onClick={handleCancelWave}>
                取消波次
              </Button>
            </Form.Item>
          </Form>
        </Space>
      </Card>
    </Space>
  );
}

function splitIds(value: string): string[] {
  return value
    .split(',')
    .map((id) => id.trim())
    .filter(Boolean);
}
