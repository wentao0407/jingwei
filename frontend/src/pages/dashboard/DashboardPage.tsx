import {
  AlertOutlined,
  AuditOutlined,
  CalendarOutlined,
  CheckCircleOutlined,
  SendOutlined,
} from '@ant-design/icons';
import { ProCard, StatisticCard } from '@ant-design/pro-components';
import { Col, Row, Space, Table, Tag, Timeline, Typography } from 'antd';

const overviewItems = [
  {
    title: '待审批',
    value: 18,
    icon: <AuditOutlined />,
  },
  {
    title: '生产中',
    value: 64,
    icon: <CalendarOutlined />,
  },
  {
    title: '库存预警',
    value: 7,
    icon: <AlertOutlined />,
  },
  {
    title: '待发运',
    value: 12,
    icon: <SendOutlined />,
  },
];

const fulfillmentRows = [
  {
    id: 'SO20260506001',
    customer: '杭州云织',
    styleCode: 'JW-POLO-2418',
    quantity: '3,200',
    deliveryDate: '2026-05-18',
    status: '待审批',
    nextAction: '销售审批',
  },
  {
    id: 'SO20260505012',
    customer: '上海锦棉',
    styleCode: 'JW-DRESS-1102',
    quantity: '1,860',
    deliveryDate: '2026-05-21',
    status: '生产中',
    nextAction: '裁剪排产',
  },
  {
    id: 'SO20260504008',
    customer: '宁波海岚',
    styleCode: 'JW-COAT-0907',
    quantity: '920',
    deliveryDate: '2026-05-14',
    status: '待发运',
    nextAction: '确认发运',
  },
  {
    id: 'SO20260503019',
    customer: '苏州织造',
    styleCode: 'JW-SHIRT-3301',
    quantity: '4,500',
    deliveryDate: '2026-05-29',
    status: '库存预警',
    nextAction: '库存复核',
  },
];

export function DashboardPage() {
  return (
    <div className="dashboard-page quiet-order-page">
      <section className="order-page-topbar">
        <div>
          <h1>工作台首页</h1>
          <p>集中查看订单、生产、库存、审批和发运待办</p>
        </div>
        <Space className="order-page-toolbar" wrap>
          <Tag icon={<CheckCircleOutlined />} color="success">
            今日已同步
          </Tag>
          <Typography.Text type="secondary">2026-05-14</Typography.Text>
        </Space>
      </section>
      <section className="order-stat-grid">
        {overviewItems.map((item) => (
          <StatisticCard key={item.title} statistic={item} />
        ))}
      </section>
      <Row gutter={[16, 16]}>
        <Col xs={24} xl={16}>
          <ProCard className="order-table-card" title="履约待办" bordered>
            <Table
              rowKey="id"
              dataSource={fulfillmentRows}
              pagination={false}
              columns={[
                { title: '订单号', dataIndex: 'id' },
                { title: '客户', dataIndex: 'customer' },
                { title: '款号', dataIndex: 'styleCode' },
                { title: '数量', dataIndex: 'quantity' },
                { title: '交期', dataIndex: 'deliveryDate' },
                {
                  title: '状态',
                  dataIndex: 'status',
                  render: (status) => <OrderStatusTag status={String(status)} />,
                },
                { title: '下一步', dataIndex: 'nextAction' },
              ]}
            />
          </ProCard>
        </Col>
        <Col xs={24} xl={8}>
          <ProCard className="order-table-card" title="今日重点" bordered>
            <Timeline
              items={[
                { color: 'blue', children: '销售订单审批 18 单' },
                { color: 'orange', children: '库存低水位 7 项' },
                { color: 'green', children: '待确认发运 12 单' },
              ]}
            />
          </ProCard>
        </Col>
      </Row>
    </div>
  );
}

function OrderStatusTag({ status }: { status: string }) {
  const colorByStatus: Record<string, string> = {
    待审批: 'blue',
    生产中: 'green',
    待发运: 'gold',
    库存预警: 'red',
  };

  return <Tag color={colorByStatus[status] ?? 'default'}>{status}</Tag>;
}
