import { CheckCircleOutlined, FileSearchOutlined, SearchOutlined } from '@ant-design/icons';
import { App, Button, Form, Input, Modal, Space, Table, Tag } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useCallback, useEffect, useState } from 'react';
import { getApiErrorMessage } from '@/services/http/apiClient';
import { confirmOutbound, getOutboundDetail, pageOutboundOrders, type InventoryOrderLineRecord, type OutboundOrderRecord } from '@/services/inventory/inventoryService';

const CONFIRMABLE_OUTBOUND_STATUSES = new Set(['DRAFT', 'CONFIRMED', 'PICKING']);
const OUTBOUND_CONFIRM_STATUS_MESSAGE = '只有草稿/已确认/拣货中状态的出库单允许发货确认';

export function OutboundOrderPage() {
  const { message, modal } = App.useApp();
  const [form] = Form.useForm<{ outboundNo?: string; status?: string; warehouseId?: string }>();
  const [records, setRecords] = useState<OutboundOrderRecord[]>([]);
  const [detail, setDetail] = useState<OutboundOrderRecord | null>(null);
  const [loading, setLoading] = useState(false);

  const loadData = useCallback(async () => {
    setLoading(true);
    try {
      const values = form.getFieldsValue();
      const result = await pageOutboundOrders({ current: 1, size: 10, outboundNo: values.outboundNo?.trim(), status: values.status, warehouseId: values.warehouseId?.trim() });
      setRecords(result.records ?? []);
    } catch (error) {
      message.error(error instanceof Error ? error.message : '查询出库单失败');
    } finally {
      setLoading(false);
    }
  }, [form, message]);

  useEffect(() => { void loadData(); }, [loadData]);

  const openDetail = async (record: OutboundOrderRecord) => setDetail(await getOutboundDetail(record.id));
  const handleConfirm = async (record: OutboundOrderRecord) => {
    if (record.status && !CONFIRMABLE_OUTBOUND_STATUSES.has(record.status)) {
      modal.warning({
        title: '无法确认出库',
        content: OUTBOUND_CONFIRM_STATUS_MESSAGE,
      });
      return;
    }

    try {
      await confirmOutbound(record.id);
      message.success('出库已确认');
      await loadData();
    } catch (error) {
      modal.warning({
        title: '无法确认出库',
        content: getApiErrorMessage(error),
      });
    }
  };

  const columns: ColumnsType<OutboundOrderRecord> = [
    { title: '出库单号', dataIndex: 'outboundNo', render: (value) => value || '-' },
    { title: '仓库', dataIndex: 'warehouseName', render: (value) => value || '-' },
    { title: '状态', dataIndex: 'statusLabel', render: renderStatus },
    { title: '出库类型', dataIndex: 'outboundTypeLabel', render: (value) => value || '-' },
    {
      title: '操作',
      render: (_, record) => (
        <Space>
          <Button size="small" icon={<FileSearchOutlined />} onClick={() => openDetail(record)} aria-label={`详情 ${record.outboundNo}`}>详情</Button>
          <Button size="small" icon={<CheckCircleOutlined />} onClick={() => handleConfirm(record)} aria-label={`确认 ${record.outboundNo}`}>确认</Button>
        </Space>
      ),
    },
  ];

  return (
    <Space direction="vertical" size={16} style={{ width: '100%' }}>
      <Form form={form} layout="inline">
        <Form.Item name="outboundNo"><Input placeholder="出库单号" allowClear /></Form.Item>
        <Form.Item name="warehouseId"><Input placeholder="仓库ID" allowClear /></Form.Item>
        <Form.Item><Button icon={<SearchOutlined />} onClick={loadData}>搜索</Button></Form.Item>
      </Form>
      <Table rowKey="id" columns={columns} dataSource={records} loading={loading} pagination={false} />
      <Modal title="出库单详情" open={Boolean(detail)} onCancel={() => setDetail(null)} footer={null} destroyOnHidden>
        <Table rowKey="id" dataSource={detail?.lines ?? []} pagination={false} columns={lineColumns} />
      </Modal>
    </Space>
  );
}

const lineColumns: ColumnsType<InventoryOrderLineRecord> = [
  { title: '物料/SKU', render: (_, record) => record.materialName || record.skuCode || '-' },
  { title: '计划', dataIndex: 'plannedQty' },
  { title: '实际', dataIndex: 'actualQty' },
];

function renderStatus(value?: string | null) {
  return value ? <Tag color="processing">{value}</Tag> : '-';
}
