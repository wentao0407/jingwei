import { EnvironmentOutlined, FileSearchOutlined, ReloadOutlined } from '@ant-design/icons';
import { App, Button, Card, Descriptions, Form, Input, Modal, Radio, Space, Table, Tag, Typography } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useState } from 'react';
import {
  confirmPutaway,
  getReceivingDetail,
  suggestReceivingLocations,
  type PutawayLocationRecord,
  type ReceivingLineRecord,
  type ReceivingOrderRecord,
} from '@/services/warehouse/receivingService';

const DEFAULT_QUERY_FORM = { receivingId: '' };

export function PutawayManagementPage() {
  const { message } = App.useApp();
  const [queryForm] = Form.useForm<typeof DEFAULT_QUERY_FORM>();
  const [putawayForm] = Form.useForm<{ locationId?: string }>();
  const [detail, setDetail] = useState<ReceivingOrderRecord | null>(null);
  const [selectedLine, setSelectedLine] = useState<ReceivingLineRecord | null>(null);
  const [suggestedLocations, setSuggestedLocations] = useState<PutawayLocationRecord[]>([]);
  const [loading, setLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  const columns: ColumnsType<ReceivingLineRecord> = [
    { title: '物料', dataIndex: 'materialName', render: (value) => value || '-' },
    { title: '实收', dataIndex: 'receivedQty', width: 100, render: (value) => value ?? 0 },
    { title: '批次', dataIndex: 'batchNo', width: 150, render: (value) => value || '-' },
    { title: '上架状态', dataIndex: 'putawayStatusLabel', width: 120, render: renderStatus },
    { title: '已上架库位', dataIndex: 'putawayLocationCode', width: 140, render: (value) => value || '-' },
    {
      title: '操作',
      key: 'action',
      width: 120,
      render: (_, record) => (
        <Button
          size="small"
          icon={<EnvironmentOutlined />}
          disabled={Number(record.receivedQty ?? 0) <= 0 || record.putawayStatus === 'COMPLETED'}
          onClick={() => openPutawayModal(record)}
          aria-label={`上架 ${record.id}`}
        >
          上架
        </Button>
      ),
    },
  ];

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
      message.error(error instanceof Error ? error.message : '查询待上架明细失败');
    } finally {
      setLoading(false);
    }
  };

  const openPutawayModal = async (line: ReceivingLineRecord) => {
    setSelectedLine(line);
    putawayForm.resetFields();
    setSuggestedLocations([]);
    setLoading(true);
    try {
      const locations = await suggestReceivingLocations(line.id);
      setSuggestedLocations(locations);
    } catch (error) {
      message.error(error instanceof Error ? error.message : '推荐上架库位失败');
    } finally {
      setLoading(false);
    }
  };

  const handlePutaway = async () => {
    if (!selectedLine) return;

    const values = await putawayForm.validateFields().catch(() => null);
    if (!values?.locationId) return;

    setSubmitting(true);
    try {
      await confirmPutaway({
        receivingLineId: selectedLine.id,
        locationId: values.locationId,
      });
      message.success('上架已确认');
      setSelectedLine(null);
      if (detail?.id) await loadDetail(detail.id);
    } catch (error) {
      message.error(error instanceof Error ? error.message : '确认上架失败');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Space direction="vertical" size={16} style={{ width: '100%' }}>
      <Card title="查询待上架明细" extra={<FileSearchOutlined />}>
        <Form form={queryForm} layout="inline" initialValues={DEFAULT_QUERY_FORM}>
          <Form.Item label="收货单 ID" name="receivingId" rules={[{ required: true, message: '请输入收货单 ID' }]}>
            <Input placeholder="收货单 ID" allowClear />
          </Form.Item>
          <Form.Item>
            <Button icon={<ReloadOutlined />} loading={loading} onClick={handleQuery}>
              查询待上架明细
            </Button>
          </Form.Item>
        </Form>
      </Card>

      {detail ? (
        <Card title={detail.receivingNo || `收货单 ${detail.id}`}>
          <Descriptions size="small" column={{ xs: 1, md: 2, xl: 4 }}>
            <Descriptions.Item label="仓库">{detail.warehouseName || detail.warehouseId || '-'}</Descriptions.Item>
            <Descriptions.Item label="状态">{renderStatus(detail.statusLabel || detail.status)}</Descriptions.Item>
            <Descriptions.Item label="收货日期">{detail.receivingDate || '-'}</Descriptions.Item>
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
          <Typography.Text type="secondary">查询收货单后，可为已收货行推荐库位并确认上架。</Typography.Text>
        </Card>
      )}

      <Modal
        title="确认上架"
        open={Boolean(selectedLine)}
        onCancel={() => setSelectedLine(null)}
        onOk={handlePutaway}
        okText="确认上架"
        confirmLoading={submitting}
        destroyOnHidden
      >
        <Form form={putawayForm} layout="vertical">
          <Form.Item label="推荐库位" name="locationId" rules={[{ required: true, message: '请选择上架库位' }]}>
            <Radio.Group style={{ width: '100%' }}>
              <Space direction="vertical" style={{ width: '100%' }}>
                {suggestedLocations.map((location) => (
                  <Radio key={location.locationId} value={location.locationId} aria-label={location.fullCode}>
                    <Space>
                      <span>{location.fullCode}</span>
                      <Tag>{location.locationType || 'STORAGE'}</Tag>
                      <Typography.Text type="secondary">
                        剩余 {location.remainingCapacity ?? '-'}
                      </Typography.Text>
                    </Space>
                  </Radio>
                ))}
              </Space>
            </Radio.Group>
          </Form.Item>
        </Form>
      </Modal>
    </Space>
  );
}

function renderStatus(value?: string | null) {
  return value ? <Tag color="processing">{value}</Tag> : '-';
}
