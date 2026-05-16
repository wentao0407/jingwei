import { EyeOutlined, ReloadOutlined } from '@ant-design/icons';
import { App, Button, Card, Form, Input, InputNumber, Modal, Space, Table, Tag, type TablePaginationConfig } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useEffect, useState } from 'react';
import { getAuthSession } from '@/shared/storage/authSessionStorage';
import {
  approveReturnOrder,
  confirmReturnReceive,
  getReturnOrderDetail,
  pageReturnOrders,
  processReturnQc,
  rejectReturnOrder,
  submitReturnOrder,
  type ReturnOrderLineRecord,
  type ReturnOrderRecord,
} from '@/services/order/returnOrderService';

const DEFAULT_PAGE_SIZE = 20;

export function ReturnOrderListPage() {
  const { message } = App.useApp();
  const [form] = Form.useForm<{ passedQty: number; failedQty: number; qcResult?: string; remark?: string }>();
  const [records, setRecords] = useState<ReturnOrderRecord[]>([]);
  const [total, setTotal] = useState(0);
  const [current, setCurrent] = useState(1);
  const [loading, setLoading] = useState(false);
  const [detail, setDetail] = useState<ReturnOrderRecord | null>(null);
  const [detailOpen, setDetailOpen] = useState(false);
  const [qcOpen, setQcOpen] = useState(false);
  const permissions = getAuthSession()?.permissions ?? [];

  useEffect(() => {
    void loadReturns(1);
  }, []);

  const loadReturns = async (page: number) => {
    setLoading(true);
    try {
      const result = await pageReturnOrders({ current: page, size: DEFAULT_PAGE_SIZE });
      setRecords(result.records ?? []);
      setTotal(result.total ?? 0);
      setCurrent(result.current ?? page);
    } finally {
      setLoading(false);
    }
  };

  const handleTableChange = (pagination: TablePaginationConfig) => {
    void loadReturns(pagination.current ?? 1);
  };

  async function openDetail(record: ReturnOrderRecord) {
    setDetail(await getReturnOrderDetail(record.id));
    setDetailOpen(true);
  }

  async function runAction(action: () => Promise<void>, successText: string) {
    await action();
    message.success(successText);
    if (detail) {
      setDetail(await getReturnOrderDetail(detail.id));
    }
    await loadReturns(current);
  }

  async function handleSubmitQc() {
    if (!detail?.lines?.[0]) {
      return;
    }
    const values = await form.validateFields().catch(() => null);
    if (!values) {
      return;
    }
    await processReturnQc({
      returnId: detail.id,
      results: [{
        lineId: detail.lines[0].id,
        passedQty: values.passedQty,
        failedQty: values.failedQty,
        qcResult: values.qcResult?.trim(),
        remark: values.remark?.trim(),
      }],
    });
    message.success('退货质检成功');
    setQcOpen(false);
    setDetail(await getReturnOrderDetail(detail.id));
    await loadReturns(current);
  }

  return (
    <Card
      title="退货单"
      extra={
        <Button icon={<ReloadOutlined />} loading={loading} onClick={() => void loadReturns(current)}>
          刷新
        </Button>
      }
    >
      <Table<ReturnOrderRecord>
        rowKey="id"
        loading={loading}
        columns={buildReturnOrderColumns(openDetail)}
        dataSource={records}
        pagination={{ current, pageSize: DEFAULT_PAGE_SIZE, total, showSizeChanger: false }}
        onChange={handleTableChange}
      />
      <ReturnDetailModal
        detail={detail}
        open={detailOpen}
        permissions={permissions}
        onCancel={() => setDetailOpen(false)}
        onApprove={() => detail ? runAction(() => approveReturnOrder(detail.id), '审批通过成功') : undefined}
        onReceive={() => detail ? runAction(() => confirmReturnReceive(detail.id), '确认收货成功') : undefined}
        onReject={() => detail ? runAction(() => rejectReturnOrder(detail.id), '审批驳回成功') : undefined}
        onSubmit={() => detail ? runAction(() => submitReturnOrder(detail.id), '提交审批成功') : undefined}
        onQc={() => {
          form.setFieldsValue({});
          setQcOpen(true);
        }}
      />
      <Modal getContainer={false} okText="提交质检" onCancel={() => setQcOpen(false)} onOk={handleSubmitQc} open={qcOpen} title="退货质检">
        <Form form={form} layout="vertical">
          <Form.Item label="合格数量" name="passedQty" rules={[{ required: true, message: '请输入合格数量' }]}><InputNumber min={0} /></Form.Item>
          <Form.Item label="不合格数量" name="failedQty" rules={[{ required: true, message: '请输入不合格数量' }]}><InputNumber min={0} /></Form.Item>
          <Form.Item label="质检结论" name="qcResult"><Input /></Form.Item>
          <Form.Item label="备注" name="remark"><Input.TextArea rows={3} /></Form.Item>
        </Form>
      </Modal>
    </Card>
  );
}

function buildReturnOrderColumns(onDetail: (record: ReturnOrderRecord) => void): ColumnsType<ReturnOrderRecord> {
  return [
  { title: '退货单号', dataIndex: 'returnNo', key: 'returnNo', render: renderText },
  { title: '销售订单', dataIndex: 'salesOrderNo', key: 'salesOrderNo', render: renderText },
  { title: '客户 ID', dataIndex: 'customerId', key: 'customerId', render: renderText },
  { title: '类型', dataIndex: 'returnTypeLabel', key: 'returnTypeLabel', render: renderText },
  { title: '数量', dataIndex: 'totalQuantity', key: 'totalQuantity', render: (value?: number | null) => value ?? 0 },
  {
    title: '状态',
    dataIndex: 'statusLabel',
    key: 'statusLabel',
    render: (value?: string | null, record?: ReturnOrderRecord) => <Tag>{value || record?.status || '-'}</Tag>,
  },
  { title: '创建时间', dataIndex: 'createdAt', key: 'createdAt', render: renderText },
  {
    title: '操作',
    key: 'actions',
    render: (_, record) => (
      <Button icon={<EyeOutlined />} size="small" aria-label={`详情 ${record.returnNo}`} onClick={() => onDetail(record)}>详情</Button>
    ),
  },
  ];
}

function ReturnDetailModal({
  detail,
  open,
  permissions,
  onApprove,
  onCancel,
  onQc,
  onReceive,
  onReject,
  onSubmit,
}: {
  detail: ReturnOrderRecord | null;
  open: boolean;
  permissions: string[];
  onApprove: () => void | Promise<void>;
  onCancel: () => void;
  onQc: () => void;
  onReceive: () => void | Promise<void>;
  onReject: () => void | Promise<void>;
  onSubmit: () => void | Promise<void>;
}) {
  return (
    <Modal footer={null} getContainer={false} onCancel={onCancel} open={open} title={detail?.returnNo ?? '退货详情'} width={900}>
      <Space direction="vertical" style={{ width: '100%' }}>
        <Space wrap>
          {permissions.includes('order:return:submit') ? <Button onClick={onSubmit}>提交审批</Button> : null}
          {permissions.includes('order:return:approve') ? <Button onClick={onApprove}>审批通过</Button> : null}
          {permissions.includes('order:return:approve') ? <Button danger onClick={onReject}>审批驳回</Button> : null}
          {permissions.includes('order:return:receive') ? <Button onClick={onReceive}>确认收货</Button> : null}
          {permissions.includes('order:return:qc') ? <Button onClick={onQc}>退货质检</Button> : null}
        </Space>
        <Table<ReturnOrderLineRecord> rowKey="id" size="small" pagination={false} columns={returnLineColumns} dataSource={detail?.lines ?? []} />
      </Space>
    </Modal>
  );
}

const returnLineColumns: ColumnsType<ReturnOrderLineRecord> = [
  { title: '行ID', dataIndex: 'id', key: 'id' },
  { title: 'SPU', dataIndex: 'spuId', key: 'spuId', render: renderText },
  { title: '颜色', dataIndex: 'colorWayId', key: 'colorWayId', render: renderText },
  { title: '退货数量', dataIndex: 'totalQuantity', key: 'totalQuantity', render: (value?: number | null) => value ?? 0 },
  { title: '合格', dataIndex: 'qcPassedQty', key: 'qcPassedQty', render: (value?: number | null) => value ?? 0 },
  { title: '不合格', dataIndex: 'qcFailedQty', key: 'qcFailedQty', render: (value?: number | null) => value ?? 0 },
];

function renderText(value?: string | null) {
  return value || '-';
}
