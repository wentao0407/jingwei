import { CheckCircleOutlined, FileSearchOutlined, SearchOutlined } from '@ant-design/icons';
import { App, Button, Form, Input, Modal, Space, Table, Tag } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useCallback, useEffect, useState } from 'react';
import { confirmInbound, getInboundDetail, pageInboundOrders, type InboundOrderRecord, type InventoryOrderLineRecord } from '@/services/inventory/inventoryService';

export function InboundOrderPage() {
  const { message } = App.useApp();
  const [form] = Form.useForm<{ inboundNo?: string; status?: string; warehouseId?: string }>();
  const [records, setRecords] = useState<InboundOrderRecord[]>([]);
  const [detail, setDetail] = useState<InboundOrderRecord | null>(null);
  const [loading, setLoading] = useState(false);

  const loadData = useCallback(async () => {
    setLoading(true);
    try {
      const values = form.getFieldsValue();
      const result = await pageInboundOrders({ current: 1, size: 10, inboundNo: values.inboundNo?.trim(), status: values.status, warehouseId: values.warehouseId?.trim() });
      setRecords(result.records ?? []);
    } catch (error) {
      message.error(error instanceof Error ? error.message : '查询入库单失败');
    } finally {
      setLoading(false);
    }
  }, [form, message]);

  useEffect(() => { void loadData(); }, [loadData]);

  const openDetail = async (record: InboundOrderRecord) => setDetail(await getInboundDetail(record.id));
  const handleConfirm = async (record: InboundOrderRecord) => {
    await confirmInbound(record.id);
    message.success('入库已确认');
    await loadData();
  };

  const columns: ColumnsType<InboundOrderRecord> = [
    { title: '入库单号', dataIndex: 'inboundNo', render: (value) => value || '-' },
    { title: '仓库', dataIndex: 'warehouseName', render: (value) => value || '-' },
    { title: '状态', dataIndex: 'statusLabel', render: renderStatus },
    { title: '入库类型', dataIndex: 'inboundTypeLabel', render: (value) => value || '-' },
    {
      title: '操作',
      render: (_, record) => (
        <Space>
          <Button size="small" icon={<FileSearchOutlined />} onClick={() => openDetail(record)} aria-label={`详情 ${record.inboundNo}`}>详情</Button>
          <Button size="small" icon={<CheckCircleOutlined />} onClick={() => handleConfirm(record)} aria-label={`确认 ${record.inboundNo}`}>确认</Button>
        </Space>
      ),
    },
  ];

  return (
    <Space direction="vertical" size={16} style={{ width: '100%' }}>
      <Form form={form} layout="inline">
        <Form.Item name="inboundNo"><Input placeholder="入库单号" allowClear /></Form.Item>
        <Form.Item name="warehouseId"><Input placeholder="仓库ID" allowClear /></Form.Item>
        <Form.Item><Button icon={<SearchOutlined />} onClick={loadData}>搜索</Button></Form.Item>
      </Form>
      <Table rowKey="id" columns={columns} dataSource={records} loading={loading} pagination={false} />
      <Modal title="入库单详情" open={Boolean(detail)} onCancel={() => setDetail(null)} footer={null} destroyOnHidden>
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
