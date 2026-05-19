import { FileSearchOutlined, PlayCircleOutlined, SaveOutlined, SearchOutlined } from '@ant-design/icons';
import { App, Button, Form, Input, InputNumber, Modal, Space, Table, Tag } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useCallback, useEffect, useState } from 'react';
import { getApiErrorMessage } from '@/services/http/apiClient';
import { getStocktakingDetail, pageStocktakingOrders, recordStocktakingCount, startStocktaking, type StocktakingLineRecord, type StocktakingOrderRecord } from '@/services/inventory/inventoryService';

const STOCKTAKING_START_STATUS_MESSAGE = '只有草稿状态的盘点单允许开始盘点';

export function StocktakingPage() {
  const { message, modal } = App.useApp();
  const [form] = Form.useForm<{ warehouseId?: string; status?: string }>();
  const [countForm] = Form.useForm<{ actualQty?: number }>();
  const [records, setRecords] = useState<StocktakingOrderRecord[]>([]);
  const [detail, setDetail] = useState<StocktakingOrderRecord | null>(null);
  const [selectedLine, setSelectedLine] = useState<StocktakingLineRecord | null>(null);
  const [loading, setLoading] = useState(false);

  const loadData = useCallback(async () => {
    setLoading(true);
    try {
      const values = form.getFieldsValue();
      const result = await pageStocktakingOrders({ current: 1, size: 10, warehouseId: values.warehouseId?.trim(), status: values.status });
      setRecords(result.records ?? []);
    } finally {
      setLoading(false);
    }
  }, [form]);

  useEffect(() => { void loadData(); }, [loadData]);

  const openDetail = async (record: StocktakingOrderRecord) => setDetail(await getStocktakingDetail(record.id));
  const handleStart = async (record: StocktakingOrderRecord) => {
    if (record.status && record.status !== 'DRAFT') {
      modal.warning({
        title: '无法开始盘点',
        content: STOCKTAKING_START_STATUS_MESSAGE,
      });
      return;
    }

    try {
      await startStocktaking(record.id);
      message.success('盘点已开始');
      await loadData();
    } catch (error) {
      modal.warning({
        title: '无法开始盘点',
        content: getApiErrorMessage(error),
      });
    }
  };
  const handleRecord = async () => {
    if (!detail || !selectedLine) return;
    const values = await countForm.validateFields().catch(() => null);
    if (!values?.actualQty && values?.actualQty !== 0) return;
    await recordStocktakingCount({ stocktakingId: detail.id, lineId: selectedLine.id, actualQty: values.actualQty });
    message.success('实盘数量已保存');
    setSelectedLine(null);
  };

  const columns: ColumnsType<StocktakingOrderRecord> = [
    { title: '盘点单号', dataIndex: 'stocktakingNo', render: (value) => value || '-' },
    { title: '仓库', dataIndex: 'warehouseName', render: (value) => value || '-' },
    { title: '状态', dataIndex: 'statusLabel', render: renderStatus },
    {
      title: '操作',
      render: (_, record) => (
        <Space>
          <Button size="small" icon={<FileSearchOutlined />} onClick={() => openDetail(record)} aria-label={`详情 ${record.stocktakingNo}`}>详情</Button>
          <Button size="small" icon={<PlayCircleOutlined />} onClick={() => handleStart(record)} aria-label={`开始 ${record.stocktakingNo}`}>开始</Button>
        </Space>
      ),
    },
  ];

  const lineColumns: ColumnsType<StocktakingLineRecord> = [
    { title: '物料/SKU', render: (_, record) => record.materialName || record.skuCode || '-' },
    { title: '系统数', dataIndex: 'systemQty', render: (value) => value ?? '-' },
    { title: '实盘数', dataIndex: 'actualQty', render: (value) => value ?? '-' },
    { title: '操作', render: (_, record) => <Button size="small" icon={<SaveOutlined />} onClick={() => setSelectedLine(record)} aria-label={`实盘 ${record.id}`}>实盘</Button> },
  ];

  return (
    <Space direction="vertical" size={16} style={{ width: '100%' }}>
      <Form form={form} layout="inline">
        <Form.Item name="warehouseId"><Input placeholder="仓库ID" allowClear /></Form.Item>
        <Form.Item><Button icon={<SearchOutlined />} onClick={loadData}>搜索</Button></Form.Item>
      </Form>
      <Table rowKey="id" columns={columns} dataSource={records} loading={loading} pagination={false} />
      <Modal title="盘点单详情" open={Boolean(detail)} onCancel={() => setDetail(null)} footer={null} destroyOnHidden>
        <Table rowKey="id" dataSource={detail?.lines ?? []} pagination={false} columns={lineColumns} />
      </Modal>
      <Modal title="录入实盘" open={Boolean(selectedLine)} onCancel={() => setSelectedLine(null)} onOk={handleRecord} okText="保存实盘" destroyOnHidden>
        <Form form={countForm} layout="vertical">
          <Form.Item label="实盘数量" name="actualQty" rules={[{ required: true, message: '请输入实盘数量' }]}>
            <InputNumber min={0} precision={2} style={{ width: '100%' }} />
          </Form.Item>
        </Form>
      </Modal>
    </Space>
  );
}

function renderStatus(value?: string | null) {
  return value ? <Tag color="processing">{value}</Tag> : '-';
}
