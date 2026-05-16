import { FileSearchOutlined, SearchOutlined, SendOutlined } from '@ant-design/icons';
import { App, Button, Card, Form, Input, Modal, Select, Space, Table, Tag } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useCallback, useEffect, useState } from 'react';
import type { InventoryOrderLineRecord, OutboundOrderRecord } from '@/services/inventory/inventoryService';
import { confirmShipment, getShipmentDetail, pageShipments } from '@/services/warehouse/shipmentService';

interface ShipmentFilterValues {
  outboundNo?: string;
  status?: string;
  warehouseId?: string;
}

const statusOptions = [
  { label: '全部状态', value: '' },
  { label: '草稿', value: 'DRAFT' },
  { label: '已确认', value: 'CONFIRMED' },
  { label: '已发运', value: 'SHIPPED' },
  { label: '已取消', value: 'CANCELLED' },
];

export function ShipmentPage() {
  const { message } = App.useApp();
  const [form] = Form.useForm<ShipmentFilterValues>();
  const [records, setRecords] = useState<OutboundOrderRecord[]>([]);
  const [detail, setDetail] = useState<OutboundOrderRecord | null>(null);
  const [loading, setLoading] = useState(false);
  const [submittingId, setSubmittingId] = useState('');

  const loadData = useCallback(async () => {
    setLoading(true);
    try {
      const values = form.getFieldsValue();
      const result = await pageShipments({
        current: 1,
        size: 20,
        outboundNo: values.outboundNo?.trim(),
        status: values.status,
        warehouseId: values.warehouseId?.trim(),
      });
      setRecords(result.records ?? []);
    } catch (error) {
      message.error(error instanceof Error ? error.message : '查询发运单失败');
    } finally {
      setLoading(false);
    }
  }, [form, message]);

  useEffect(() => {
    void loadData();
  }, [loadData]);

  const openDetail = async (record: OutboundOrderRecord) => {
    try {
      setDetail(await getShipmentDetail(record.id));
    } catch (error) {
      message.error(error instanceof Error ? error.message : '查询发运详情失败');
    }
  };

  const handleConfirmShipment = async (record: OutboundOrderRecord) => {
    setSubmittingId(record.id);
    try {
      await confirmShipment({
        outboundId: record.id,
        salesOrderId: record.sourceType === 'SALES_ORDER' ? record.sourceId ?? undefined : undefined,
      });
      message.success('发运已确认');
      await loadData();
    } catch (error) {
      message.error(error instanceof Error ? error.message : '确认发运失败');
    } finally {
      setSubmittingId('');
    }
  };

  const columns = buildColumns({
    onConfirm: handleConfirmShipment,
    onOpenDetail: openDetail,
    submittingId,
  });

  return (
    <Space direction="vertical" size={16} style={{ width: '100%' }}>
      <Card title="发运单">
        <Form<ShipmentFilterValues> form={form} layout="inline">
          <Form.Item name="outboundNo">
            <Input placeholder="出库单号" allowClear />
          </Form.Item>
          <Form.Item name="warehouseId">
            <Input placeholder="仓库 ID" allowClear />
          </Form.Item>
          <Form.Item name="status">
            <Select aria-label="状态" options={statusOptions} style={{ width: 140 }} />
          </Form.Item>
          <Form.Item>
            <Button icon={<SearchOutlined />} loading={loading} onClick={() => void loadData()}>搜索</Button>
          </Form.Item>
        </Form>
      </Card>
      <Table<OutboundOrderRecord> rowKey="id" columns={columns} dataSource={records} loading={loading} pagination={false} />
      <Modal title="发运单详情" open={Boolean(detail)} onCancel={() => setDetail(null)} footer={null} width={820} destroyOnHidden>
        <Space direction="vertical" size={12} style={{ width: '100%' }}>
          <Card size="small">
            <Space wrap>
              <span>出库单号：{detail?.outboundNo || '-'}</span>
              <span>来源单号：{detail?.sourceNo || '-'}</span>
              <span>承运商：{detail?.carrier || '-'}</span>
              <span>物流单号：{detail?.trackingNo || '-'}</span>
            </Space>
          </Card>
          <Table<InventoryOrderLineRecord> rowKey={getLineRowKey} dataSource={detail?.lines ?? []} pagination={false} columns={lineColumns} />
        </Space>
      </Modal>
    </Space>
  );
}

function buildColumns(handlers: {
  onConfirm: (record: OutboundOrderRecord) => void;
  onOpenDetail: (record: OutboundOrderRecord) => void;
  submittingId: string;
}): ColumnsType<OutboundOrderRecord> {
  return [
    { title: '出库单号', dataIndex: 'outboundNo', render: renderText },
    { title: '仓库', dataIndex: 'warehouseName', render: renderText },
    { title: '状态', dataIndex: 'statusLabel', render: renderStatus },
    { title: '来源单号', dataIndex: 'sourceNo', render: renderText },
    { title: '承运商', dataIndex: 'carrier', render: renderText },
    { title: '物流单号', dataIndex: 'trackingNo', render: renderText },
    {
      title: '操作',
      render: (_, record) => (
        <Space>
          <Button size="small" icon={<FileSearchOutlined />} aria-label={`详情 ${record.outboundNo}`} onClick={() => handlers.onOpenDetail(record)}>
            详情
          </Button>
          {record.status !== 'SHIPPED' ? (
            <Button
              size="small"
              type="primary"
              icon={<SendOutlined />}
              loading={handlers.submittingId === record.id}
              aria-label={`确认发运 ${record.outboundNo}`}
              onClick={() => handlers.onConfirm(record)}
            >
              确认发运
            </Button>
          ) : null}
        </Space>
      ),
    },
  ];
}

const lineColumns: ColumnsType<InventoryOrderLineRecord> = [
  { title: '物料/SKU', render: (_, record) => record.materialName || record.skuCode || record.materialId || record.skuId || '-' },
  { title: '批次', dataIndex: 'batchNo', render: renderText },
  { title: '计划', dataIndex: 'plannedQty', render: renderText },
  { title: '实际', dataIndex: 'actualQty', render: renderText },
];

function renderText(value?: string | number | null) {
  return value ?? '-';
}

function renderStatus(value?: string | null) {
  return value ? <Tag color="processing">{value}</Tag> : '-';
}

function getLineRowKey(record: InventoryOrderLineRecord) {
  return record.id || `${record.inventoryType || 'line'}-${record.skuId || record.materialId || 'item'}-${record.batchNo || 'batch'}`;
}
