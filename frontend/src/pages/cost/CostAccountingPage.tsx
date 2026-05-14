import { CalculatorOutlined, SearchOutlined } from '@ant-design/icons';
import { App, Button, Card, Descriptions, Form, Input, Space, Table, Typography } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useState } from 'react';
import {
  getCostDetail,
  getCostIssueDetails,
  type CostMaterialIssueRecord,
  type CostProductionOrderRecord,
} from '@/services/cost/costService';

const DEFAULT_QUERY = { productionOrderId: '', productionLineId: '' };

export function CostAccountingPage() {
  const { message } = App.useApp();
  const [form] = Form.useForm<typeof DEFAULT_QUERY>();
  const [costDetail, setCostDetail] = useState<CostProductionOrderRecord | null>(null);
  const [issueRows, setIssueRows] = useState<CostMaterialIssueRecord[]>([]);
  const [loading, setLoading] = useState(false);

  const handleQuery = async () => {
    const values = await form.validateFields().catch(() => null);
    if (!values) return;

    setLoading(true);
    try {
      const [detail, issues] = await Promise.all([
        getCostDetail({
          productionOrderId: values.productionOrderId.trim(),
          productionLineId: values.productionLineId.trim(),
        }),
        getCostIssueDetails(values.productionOrderId.trim()),
      ]);
      setCostDetail(detail);
      setIssueRows(issues);
    } catch (error) {
      message.error(error instanceof Error ? error.message : '查询成本失败');
    } finally {
      setLoading(false);
    }
  };

  const columns: ColumnsType<CostMaterialIssueRecord> = [
    { title: '物料类型', dataIndex: 'materialTypeLabel', render: (value) => value || '-' },
    { title: '领料数量', dataIndex: 'issueQty', render: (value) => value ?? '-' },
    { title: '单位成本', dataIndex: 'unitCost', render: (value) => value ?? '-' },
    { title: '成本金额', dataIndex: 'costAmount', render: (value) => value ?? '-' },
    { title: '领料日期', dataIndex: 'issueDate', render: (value) => value || '-' },
  ];

  return (
    <Space direction="vertical" size={16} style={{ width: '100%' }}>
      <Card title="成本核算" extra={<CalculatorOutlined />}>
        <Form form={form} layout="inline" initialValues={DEFAULT_QUERY}>
          <Form.Item label="生产订单 ID" name="productionOrderId" rules={[{ required: true, message: '请输入生产订单 ID' }]}>
            <Input placeholder="生产订单 ID" allowClear />
          </Form.Item>
          <Form.Item label="生产行 ID" name="productionLineId" rules={[{ required: true, message: '请输入生产行 ID' }]}>
            <Input placeholder="生产订单行 ID" allowClear />
          </Form.Item>
          <Form.Item>
            <Button type="primary" icon={<SearchOutlined />} loading={loading} onClick={handleQuery}>
              查询成本
            </Button>
          </Form.Item>
        </Form>
      </Card>

      {costDetail ? (
        <Card title="生产成本归集">
          <Descriptions size="small" column={{ xs: 1, md: 2, xl: 4 }}>
            <Descriptions.Item label="面料成本">{costDetail.materialCost ?? '-'}</Descriptions.Item>
            <Descriptions.Item label="辅料成本">{costDetail.trimCost ?? '-'}</Descriptions.Item>
            <Descriptions.Item label="包材成本">{costDetail.packagingCost ?? '-'}</Descriptions.Item>
            <Descriptions.Item label="总成本">{costDetail.totalCost ?? '-'}</Descriptions.Item>
            <Descriptions.Item label="完工数量">{costDetail.completedQty ?? '-'}</Descriptions.Item>
            <Descriptions.Item label="单位成本">{costDetail.unitCost ?? '-'}</Descriptions.Item>
          </Descriptions>
        </Card>
      ) : (
        <Card>
          <Typography.Text type="secondary">输入生产订单 ID 和生产行 ID 后，可查询成本归集与领料成本明细。</Typography.Text>
        </Card>
      )}

      <Card title="领料成本明细">
        <Table rowKey="id" columns={columns} dataSource={issueRows} loading={loading} pagination={false} />
      </Card>
    </Space>
  );
}
