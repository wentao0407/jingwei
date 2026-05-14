import { CheckCircleOutlined, FileSearchOutlined, InboxOutlined, PlusOutlined, ReloadOutlined } from '@ant-design/icons';
import { App, Button, Card, Col, Descriptions, Form, Input, InputNumber, Modal, Row, Space, Table, Tag, Typography } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useState } from 'react';
import {
  confirmReceive,
  createReceivingFromAsn,
  getReceivingDetail,
  type ReceivingLineRecord,
  type ReceivingOrderRecord,
} from '@/services/warehouse/receivingService';

const DEFAULT_CREATE_FORM = { asnId: '', warehouseId: '', dockNo: '' };
const DEFAULT_QUERY_FORM = { receivingId: '' };

export function ReceivingManagementPage() {
  const { message } = App.useApp();
  const [createForm] = Form.useForm<typeof DEFAULT_CREATE_FORM>();
  const [queryForm] = Form.useForm<typeof DEFAULT_QUERY_FORM>();
  const [receiveForm] = Form.useForm<{ receivedQty?: number; rollCount?: number }>();
  const [detail, setDetail] = useState<ReceivingOrderRecord | null>(null);
  const [selectedLine, setSelectedLine] = useState<ReceivingLineRecord | null>(null);
  const [loading, setLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  const columns: ColumnsType<ReceivingLineRecord> = [
    { title: '物料', dataIndex: 'materialName', render: (value) => value || '-' },
    { title: '应收', dataIndex: 'expectedQty', width: 100, render: formatQuantity },
    { title: '已收', dataIndex: 'receivedQty', width: 100, render: formatQuantity },
    { title: '差异', dataIndex: 'differenceQty', width: 100, render: formatQuantity },
    { title: '质检', dataIndex: 'qcStatusLabel', width: 100, render: renderStatus },
    { title: '上架', dataIndex: 'putawayStatusLabel', width: 100, render: renderStatus },
    {
      title: '操作',
      key: 'action',
      width: 140,
      render: (_, record) => (
        <Button
          size="small"
          icon={<CheckCircleOutlined />}
          disabled={getRemainingQuantity(record) <= 0}
          onClick={() => openReceiveModal(record)}
          aria-label={`确认收货 ${record.id}`}
        >
          确认收货
        </Button>
      ),
    },
  ];

  const handleCreate = async () => {
    const values = await createForm.validateFields().catch(() => null);
    if (!values) return;

    setSubmitting(true);
    try {
      const created = await createReceivingFromAsn({
        asnId: values.asnId.trim(),
        warehouseId: values.warehouseId.trim(),
        dockNo: values.dockNo?.trim(),
      });
      setDetail(created);
      queryForm.setFieldValue('receivingId', created.id);
      message.success('收货单已创建');
    } catch (error) {
      message.error(error instanceof Error ? error.message : '创建收货单失败');
    } finally {
      setSubmitting(false);
    }
  };

  const handleQuery = async () => {
    const values = await queryForm.validateFields().catch(() => null);
    if (!values) return;

    await loadDetail(values.receivingId.trim());
  };

  const loadDetail = async (receivingId: string) => {
    setLoading(true);
    try {
      const nextDetail = await getReceivingDetail(receivingId);
      setDetail(nextDetail);
    } catch (error) {
      message.error(error instanceof Error ? error.message : '查询收货单失败');
    } finally {
      setLoading(false);
    }
  };

  const openReceiveModal = (line: ReceivingLineRecord) => {
    setSelectedLine(line);
    receiveForm.resetFields();
  };

  const handleReceive = async () => {
    if (!selectedLine) return;

    const values = await receiveForm.validateFields().catch(() => null);
    if (!values?.receivedQty) return;

    setSubmitting(true);
    try {
      await confirmReceive({
        receivingLineId: selectedLine.id,
        receivedQty: values.receivedQty,
        rollCount: values.rollCount,
      });
      message.success('收货已确认');
      setSelectedLine(null);
      if (detail?.id) await loadDetail(detail.id);
    } catch (error) {
      message.error(error instanceof Error ? error.message : '确认收货失败');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Space direction="vertical" size={16} style={{ width: '100%' }}>
      <Row gutter={[16, 16]}>
        <Col xs={24} lg={14}>
          <Card title="从 ASN 创建收货单" extra={<InboxOutlined />}>
            <Form form={createForm} layout="inline" initialValues={DEFAULT_CREATE_FORM}>
              <Form.Item label="ASN ID" name="asnId" rules={[{ required: true, message: '请输入 ASN ID' }]}>
                <Input placeholder="ASN ID" allowClear />
              </Form.Item>
              <Form.Item label="仓库 ID" name="warehouseId" rules={[{ required: true, message: '请输入仓库 ID' }]}>
                <Input placeholder="仓库 ID" allowClear />
              </Form.Item>
              <Form.Item label="月台号" name="dockNo">
                <Input placeholder="月台号" allowClear />
              </Form.Item>
              <Form.Item>
                <Button type="primary" icon={<PlusOutlined />} loading={submitting} onClick={handleCreate}>
                  创建收货单
                </Button>
              </Form.Item>
            </Form>
          </Card>
        </Col>
        <Col xs={24} lg={10}>
          <Card title="查询收货单" extra={<FileSearchOutlined />}>
            <Form form={queryForm} layout="inline" initialValues={DEFAULT_QUERY_FORM}>
              <Form.Item label="收货单 ID" name="receivingId" rules={[{ required: true, message: '请输入收货单 ID' }]}>
                <Input placeholder="收货单 ID" allowClear />
              </Form.Item>
              <Form.Item>
                <Button icon={<ReloadOutlined />} loading={loading} onClick={handleQuery}>
                  查询收货单
                </Button>
              </Form.Item>
            </Form>
          </Card>
        </Col>
      </Row>

      {detail ? (
        <Card title={detail.receivingNo || `收货单 ${detail.id}`}>
          <Descriptions size="small" column={{ xs: 1, md: 2, xl: 4 }}>
            <Descriptions.Item label="ASN">{detail.asnNo || detail.asnId || '-'}</Descriptions.Item>
            <Descriptions.Item label="仓库">{detail.warehouseName || detail.warehouseId || '-'}</Descriptions.Item>
            <Descriptions.Item label="状态">{renderStatus(detail.statusLabel || detail.status)}</Descriptions.Item>
            <Descriptions.Item label="月台">{detail.dockNo || '-'}</Descriptions.Item>
          </Descriptions>
          <Table
            rowKey="id"
            style={{ marginTop: 16 }}
            columns={columns}
            dataSource={detail.lines ?? []}
            pagination={false}
            loading={loading}
          />
        </Card>
      ) : (
        <Card>
          <Typography.Text type="secondary">创建或查询收货单后，可在这里逐行确认实收数量。</Typography.Text>
        </Card>
      )}

      <Modal
        title="确认收货"
        open={Boolean(selectedLine)}
        onCancel={() => setSelectedLine(null)}
        onOk={handleReceive}
        okText="提交收货"
        confirmLoading={submitting}
        destroyOnHidden
      >
        <Form form={receiveForm} layout="vertical">
          <Form.Item
            label="本次实收数量"
            name="receivedQty"
            rules={[
              {
                validator: (_, value) => validateReceiveQuantity(value, selectedLine),
              },
            ]}
          >
            <InputNumber min={0} precision={2} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item label="实收卷数" name="rollCount">
            <InputNumber min={0} precision={0} style={{ width: '100%' }} />
          </Form.Item>
        </Form>
      </Modal>
    </Space>
  );
}

function getRemainingQuantity(line: ReceivingLineRecord) {
  return Number(line.expectedQty ?? 0) - Number(line.receivedQty ?? 0);
}

function validateReceiveQuantity(value: number | undefined, line: ReceivingLineRecord | null) {
  if (!value || value <= 0) {
    return Promise.reject(new Error('请输入大于 0 的实收数量'));
  }

  if (line && value > getRemainingQuantity(line)) {
    return Promise.reject(new Error('本次实收数量不能超过剩余可收数量'));
  }

  return Promise.resolve();
}

function formatQuantity(value?: number | null) {
  return value ?? 0;
}

function renderStatus(value?: string | null) {
  return value ? <Tag color="processing">{value}</Tag> : '-';
}
