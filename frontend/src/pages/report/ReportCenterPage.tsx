import { SearchOutlined } from '@ant-design/icons';
import { App, Button, Card, Form, Input, Select, Space, Statistic, Table, Tabs, Tag } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useCallback, useEffect, useState } from 'react';
import {
  pageInventoryLedger,
  pageOperationFlows,
  queryInventoryAge,
  queryTurnoverAnalysis,
  type InventoryAgeRecord,
  type InventoryAgeSummary,
  type InventoryLedgerRecord,
  type OperationFlowRecord,
  type TurnoverRecord,
} from '@/services/report/reportService';

type ReportTabKey = 'ledger' | 'flow' | 'age' | 'turnover';

const DEFAULT_QUERY = { current: 1, size: 20, inventoryType: 'SKU', keyword: '' };

export function ReportCenterPage() {
  const { message } = App.useApp();
  const [form] = Form.useForm<typeof DEFAULT_QUERY>();
  const [activeKey, setActiveKey] = useState<ReportTabKey>('ledger');
  const [loading, setLoading] = useState(false);
  const [ledgerRows, setLedgerRows] = useState<InventoryLedgerRecord[]>([]);
  const [flowRows, setFlowRows] = useState<OperationFlowRecord[]>([]);
  const [ageSummary, setAgeSummary] = useState<InventoryAgeSummary | null>(null);
  const [turnoverRows, setTurnoverRows] = useState<TurnoverRecord[]>([]);

  const loadReport = useCallback(async () => {
    const values = form.getFieldsValue();
    setLoading(true);
    try {
      if (activeKey === 'ledger') {
        const result = await pageInventoryLedger({ ...DEFAULT_QUERY, ...values });
        setLedgerRows(result.records ?? []);
      }
      if (activeKey === 'flow') {
        const result = await pageOperationFlows({ ...DEFAULT_QUERY, ...values });
        setFlowRows(result.records ?? []);
      }
      if (activeKey === 'age') {
        const result = await queryInventoryAge({ ...DEFAULT_QUERY, ...values });
        setAgeSummary(result);
      }
      if (activeKey === 'turnover') {
        const result = await queryTurnoverAnalysis({ ...DEFAULT_QUERY, ...values });
        setTurnoverRows(result.records ?? []);
      }
    } catch (error) {
      message.error(error instanceof Error ? error.message : '查询报表失败');
    } finally {
      setLoading(false);
    }
  }, [activeKey, form, message]);

  useEffect(() => {
    void loadReport();
  }, [loadReport]);

  return (
    <Space direction="vertical" size={16} style={{ width: '100%' }}>
      <Card title="报表中心">
        <Form form={form} layout="inline" initialValues={DEFAULT_QUERY} onFinish={loadReport}>
          <Form.Item label="库存类型" name="inventoryType">
            <Select style={{ width: 140 }} options={[{ label: '成品 SKU', value: 'SKU' }, { label: '物料', value: 'MATERIAL' }]} />
          </Form.Item>
          <Form.Item label="关键字" name="keyword">
            <Input placeholder="SKU/物料/单号" allowClear />
          </Form.Item>
          <Form.Item>
            <Button htmlType="submit" icon={<SearchOutlined />} loading={loading}>
              查询报表
            </Button>
          </Form.Item>
        </Form>
      </Card>
      <Card>
        <Tabs
          activeKey={activeKey}
          onChange={(key) => setActiveKey(key as ReportTabKey)}
          items={[
            { key: 'ledger', label: '库存台账' },
            { key: 'flow', label: '出入库流水' },
            { key: 'age', label: '库龄分析' },
            { key: 'turnover', label: '畅滞销分析' },
          ]}
        />
        {activeKey === 'ledger' ? <LedgerTable rows={ledgerRows} loading={loading} /> : null}
        {activeKey === 'flow' ? <FlowTable rows={flowRows} loading={loading} /> : null}
        {activeKey === 'age' ? <AgeReport summary={ageSummary} loading={loading} /> : null}
        {activeKey === 'turnover' ? <TurnoverTable rows={turnoverRows} loading={loading} /> : null}
      </Card>
    </Space>
  );
}

function LedgerTable({ rows, loading }: { rows: InventoryLedgerRecord[]; loading: boolean }) {
  const columns: ColumnsType<InventoryLedgerRecord> = [
    { title: 'SKU/物料', key: 'item', render: (_, record) => record.skuCode || record.materialName || record.materialCode || '-' },
    { title: '仓库', dataIndex: 'warehouseName', render: (value) => value || '-' },
    { title: '可用', dataIndex: 'availableQty', render: (value) => value ?? '-' },
    { title: '锁定', dataIndex: 'lockedQty', render: (value) => value ?? '-' },
    { title: '总库存', dataIndex: 'totalQty', render: (value) => value ?? '-' },
    { title: '库存金额', dataIndex: 'totalAmount', render: (value) => value ?? '-' },
  ];
  return <Table rowKey="inventoryId" columns={columns} dataSource={rows} loading={loading} pagination={false} />;
}

function FlowTable({ rows, loading }: { rows: OperationFlowRecord[]; loading: boolean }) {
  const columns: ColumnsType<OperationFlowRecord> = [
    { title: '操作单号', dataIndex: 'operationNo', render: (value) => value || '-' },
    { title: '类型', dataIndex: 'operationTypeLabel', render: (value) => value || '-' },
    { title: '库存对象', key: 'item', render: (_, record) => record.skuCode || record.materialCode || '-' },
    { title: '变动数量', dataIndex: 'changeQty', render: (value) => value ?? '-' },
  ];
  return <Table rowKey="id" columns={columns} dataSource={rows} loading={loading} pagination={false} />;
}

function AgeReport({ summary, loading }: { summary: InventoryAgeSummary | null; loading: boolean }) {
  const columns: ColumnsType<InventoryAgeRecord> = [
    { title: 'SKU/物料', key: 'item', render: (_, record) => record.skuCode || record.materialCode || '-' },
    { title: '库龄区间', dataIndex: 'ageRange', render: (value) => value || '-' },
    { title: '库龄天数', dataIndex: 'ageDays', render: (value) => value ?? '-' },
    { title: '总库存', dataIndex: 'totalQty', render: (value) => value ?? '-' },
    { title: '超期', dataIndex: 'overdue', render: (value) => <Tag color={value ? 'red' : 'green'}>{value ? '是' : '否'}</Tag> },
  ];
  return (
    <Space direction="vertical" size={16} style={{ width: '100%' }}>
      <Space>
        <Statistic title="总记录" value={summary?.totalCount ?? 0} />
        <Statistic title="超期记录" value={summary?.overdueCount ?? 0} />
        <Statistic title="总金额" value={summary?.totalAmount ?? 0} />
      </Space>
      <Table rowKey="inventoryId" columns={columns} dataSource={summary?.details ?? []} loading={loading} pagination={false} />
    </Space>
  );
}

function TurnoverTable({ rows, loading }: { rows: TurnoverRecord[]; loading: boolean }) {
  const columns: ColumnsType<TurnoverRecord> = [
    { title: 'SKU/物料', key: 'item', render: (_, record) => record.skuCode || record.materialCode || '-' },
    { title: '当前库存', dataIndex: 'currentQty', render: (value) => value ?? '-' },
    { title: '出库数量', dataIndex: 'outboundQty', render: (value) => value ?? '-' },
    { title: '周转天数', dataIndex: 'turnoverDays', render: (value) => value ?? '-' },
    { title: '等级', dataIndex: 'turnoverGradeLabel', render: (value) => value || '-' },
  ];
  return <Table rowKey={(record) => record.skuCode || record.materialCode || 'turnover'} columns={columns} dataSource={rows} loading={loading} pagination={false} />;
}
