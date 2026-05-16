import { AppstoreOutlined, DownloadOutlined, SearchOutlined } from '@ant-design/icons';
import { App, Button, Card, Form, Input, Modal, Select, Space, Statistic, Table, Tabs, Tag } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useCallback, useEffect, useState } from 'react';
import {
  exportInventoryAge,
  exportInventoryLedger,
  exportOperationFlows,
  exportTurnoverAnalysis,
  pageInventoryLedger,
  pageOperationFlows,
  queryInventoryLedgerMatrix,
  queryInventoryAge,
  queryTurnoverAnalysis,
  type InventoryAgeRecord,
  type InventoryAgeSummary,
  type InventoryLedgerMatrixRecord,
  type InventoryLedgerRecord,
  type OperationFlowRecord,
  type TurnoverRecord,
} from '@/services/report/reportService';
import {
  attachReportRowKeys,
  getAgeRowKey,
  getLedgerRowKey,
  getTurnoverRowKey,
  type ReportRowWithKey,
} from './reportRowKeys';

type ReportTabKey = 'ledger' | 'flow' | 'age' | 'turnover';

const DEFAULT_QUERY = { current: 1, size: 20, inventoryType: 'SKU', keyword: '' };

export function ReportCenterPage() {
  const { message } = App.useApp();
  const [form] = Form.useForm<typeof DEFAULT_QUERY>();
  const [matrixForm] = Form.useForm<{ spuId: string; warehouseId: string }>();
  const [activeKey, setActiveKey] = useState<ReportTabKey>('ledger');
  const [loading, setLoading] = useState(false);
  const [exporting, setExporting] = useState(false);
  const [matrixOpen, setMatrixOpen] = useState(false);
  const [matrixLoading, setMatrixLoading] = useState(false);
  const [ledgerMatrix, setLedgerMatrix] = useState<InventoryLedgerMatrixRecord | null>(null);
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

  const exportReport = useCallback(async () => {
    const values = { ...DEFAULT_QUERY, ...form.getFieldsValue() };
    setExporting(true);
    try {
      const blob = await exportActiveReport(activeKey, values);
      downloadReport(blob, `${REPORT_FILE_NAMES[activeKey]}.xlsx`);
      message.success('报表导出已开始');
    } catch (error) {
      message.error(error instanceof Error ? error.message : '导出报表失败');
    } finally {
      setExporting(false);
    }
  }, [activeKey, form, message]);

  const queryMatrix = useCallback(async () => {
    const values = await matrixForm.validateFields().catch(() => null);
    if (!values) return;

    setMatrixLoading(true);
    try {
      const matrix = await queryInventoryLedgerMatrix(values.spuId, values.warehouseId);
      setLedgerMatrix(matrix);
      if (!matrix) {
        message.warning('未查询到库存矩阵数据');
      }
    } catch (error) {
      message.error(error instanceof Error ? error.message : '查询库存矩阵失败');
    } finally {
      setMatrixLoading(false);
    }
  }, [matrixForm, message]);

  const openMatrixDialog = () => {
    setLedgerMatrix(null);
    setMatrixOpen(true);
  };

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
          <Form.Item>
            <Button icon={<DownloadOutlined />} loading={exporting} onClick={exportReport}>
              导出报表
            </Button>
          </Form.Item>
          {activeKey === 'ledger' ? (
            <Form.Item>
              <Button icon={<AppstoreOutlined />} onClick={openMatrixDialog}>
                矩阵视图
              </Button>
            </Form.Item>
          ) : null}
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
      <Modal
        title="库存台账矩阵"
        open={matrixOpen}
        onCancel={() => setMatrixOpen(false)}
        onOk={() => void queryMatrix()}
        confirmLoading={matrixLoading}
        okText="查询矩阵"
        width={840}
      >
        <Space direction="vertical" size={16} style={{ width: '100%' }}>
          <Form form={matrixForm} layout="inline">
            <Form.Item label="款式 ID" name="spuId" rules={[{ required: true, whitespace: true, message: '请输入款式 ID' }]}>
              <Input placeholder="SPU ID" allowClear />
            </Form.Item>
            <Form.Item label="仓库 ID" name="warehouseId" rules={[{ required: true, whitespace: true, message: '请输入仓库 ID' }]}>
              <Input placeholder="仓库 ID" allowClear />
            </Form.Item>
          </Form>
          {ledgerMatrix ? <LedgerMatrixTable matrix={ledgerMatrix} loading={matrixLoading} /> : null}
        </Space>
      </Modal>
    </Space>
  );
}

const REPORT_FILE_NAMES: Record<ReportTabKey, string> = {
  ledger: '库存台账',
  flow: '出入库流水',
  age: '库龄分析',
  turnover: '畅滞销分析',
};

async function exportActiveReport(key: ReportTabKey, values: typeof DEFAULT_QUERY): Promise<Blob> {
  if (key === 'ledger') {
    return exportInventoryLedger(values);
  }

  if (key === 'flow') {
    return exportOperationFlows(values);
  }

  if (key === 'age') {
    return exportInventoryAge(values);
  }

  return exportTurnoverAnalysis(values);
}

function downloadReport(blob: Blob, fileName: string) {
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = fileName;
  link.click();
  URL.revokeObjectURL(url);
}

function LedgerTable({ rows, loading }: { rows: InventoryLedgerRecord[]; loading: boolean }) {
  const dataSource = attachReportRowKeys(rows, getLedgerRowKey);
  const columns: ColumnsType<ReportRowWithKey<InventoryLedgerRecord>> = [
    { title: 'SKU/物料', key: 'item', render: (_, record) => record.skuCode || record.materialName || record.materialCode || '-' },
    { title: '仓库', dataIndex: 'warehouseName', render: (value) => value || '-' },
    { title: '可用', dataIndex: 'availableQty', render: (value) => value ?? '-' },
    { title: '锁定', dataIndex: 'lockedQty', render: (value) => value ?? '-' },
    { title: '总库存', dataIndex: 'totalQty', render: (value) => value ?? '-' },
    { title: '库存金额', dataIndex: 'totalAmount', render: (value) => value ?? '-' },
  ];
  return <Table rowKey="clientRowKey" columns={columns} dataSource={dataSource} loading={loading} pagination={false} />;
}

function LedgerMatrixTable({ matrix, loading }: { matrix: InventoryLedgerMatrixRecord; loading: boolean }) {
  const sizes = matrix.sizes ?? [];
  const rows = Object.entries(matrix.matrix ?? {}).map(([colorName, quantities]) => ({
    colorName,
    ...quantities,
    totalQty: matrix.colorTotals?.[colorName] ?? 0,
  }));
  const totalRow = {
    colorName: '合计',
    ...Object.fromEntries(sizes.map((size) => [size, matrix.sizeTotals?.[size] ?? 0])),
    totalQty: matrix.grandTotal ?? 0,
  };
  const columns: ColumnsType<Record<string, string | number>> = [
    { title: '颜色', dataIndex: 'colorName' },
    ...sizes.map((size) => ({ title: size, dataIndex: size, render: (value?: number) => value ?? 0 })),
    { title: '合计', dataIndex: 'totalQty', render: (value?: number) => value ?? 0 },
  ];

  return (
    <Space direction="vertical" size={12} style={{ width: '100%' }}>
      <Statistic title={matrix.spuName || matrix.spuCode || '款式库存'} value={matrix.grandTotal ?? 0} suffix="件" />
      <Table rowKey="colorName" columns={columns} dataSource={[...rows, totalRow]} loading={loading} pagination={false} />
    </Space>
  );
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
  const dataSource = attachReportRowKeys(summary?.details ?? [], getAgeRowKey);
  const columns: ColumnsType<ReportRowWithKey<InventoryAgeRecord>> = [
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
      <Table rowKey="clientRowKey" columns={columns} dataSource={dataSource} loading={loading} pagination={false} />
    </Space>
  );
}

function TurnoverTable({ rows, loading }: { rows: TurnoverRecord[]; loading: boolean }) {
  const dataSource = attachReportRowKeys(rows, getTurnoverRowKey);
  const columns: ColumnsType<ReportRowWithKey<TurnoverRecord>> = [
    { title: 'SKU/物料', key: 'item', render: (_, record) => record.skuCode || record.materialCode || '-' },
    { title: '当前库存', dataIndex: 'currentQty', render: (value) => value ?? '-' },
    { title: '出库数量', dataIndex: 'outboundQty', render: (value) => value ?? '-' },
    { title: '周转天数', dataIndex: 'turnoverDays', render: (value) => value ?? '-' },
    { title: '等级', dataIndex: 'turnoverGradeLabel', render: (value) => value || '-' },
  ];
  return <Table rowKey="clientRowKey" columns={columns} dataSource={dataSource} loading={loading} pagination={false} />;
}
