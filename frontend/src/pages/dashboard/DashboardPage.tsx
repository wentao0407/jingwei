import {
  AlertOutlined,
  ArrowUpOutlined,
  AuditOutlined,
  CalendarOutlined,
  CheckCircleOutlined,
  ClockCircleOutlined,
  SendOutlined,
} from '@ant-design/icons';
import { ProCard } from '@ant-design/pro-components';
import { Col, Progress, Row, Space, Table, Tag, Typography } from 'antd';

const overviewItems = [
  {
    title: '待审批',
    value: 18,
    unit: '单',
    trend: '较昨日 +3',
    tone: 'blue',
    icon: <AuditOutlined />,
  },
  {
    title: '生产中',
    value: 64,
    unit: '单',
    trend: '裁剪 12 / 缝制 38',
    tone: 'green',
    icon: <CalendarOutlined />,
  },
  {
    title: '库存预警',
    value: 7,
    unit: '项',
    trend: '2 项高风险',
    tone: 'red',
    icon: <AlertOutlined />,
  },
  {
    title: '待发运',
    value: 12,
    unit: '单',
    trend: '今日应发 5',
    tone: 'gold',
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

const focusItems = [
  {
    title: '销售订单审批',
    subtitle: '18 单等待业务负责人处理',
    tag: '高优先级',
    owner: '销售 / 财务',
    due: '今日 12:00',
    tone: 'blue',
  },
  {
    title: '库存复核',
    subtitle: '7 项低水位库存需确认替代料或补采',
    tag: '风险',
    owner: '仓储 / 采购',
    due: '今日 15:00',
    tone: 'red',
  },
  {
    title: '发运确认',
    subtitle: '12 单已完成复核，等待物流单号回填',
    tag: '待确认',
    owner: '仓储 / 物流',
    due: '今日 18:00',
    tone: 'green',
  },
];

const rhythmItems = [
  { label: '订单确认', value: 72, detail: '本周目标 80 单' },
  { label: '生产完工', value: 64, detail: '稳定推进' },
  { label: '仓储出库', value: 58, detail: '待发运拉动' },
];

function formatSyncTime(date: Date): string {
  const formatter = new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    hour12: false,
  });

  return formatter.format(date).replace(/\//g, '-');
}

export function DashboardPage() {
  const syncTime = formatSyncTime(new Date());

  return (
    <div className="dashboard-page quiet-order-page">
      <section className="dashboard-hero">
        <div>
          <h1>工作台首页</h1>
          <p>集中查看订单、生产、库存、审批和发运待办</p>
        </div>
        <div className="dashboard-sync-card">
          <span className="dashboard-sync-icon">
            <CheckCircleOutlined />
          </span>
          <div>
            <Typography.Text type="secondary">今日已同步</Typography.Text>
            <strong>{syncTime}</strong>
          </div>
        </div>
      </section>
      <section className="dashboard-stat-grid">
        {overviewItems.map((item) => (
          <ProCard key={item.title} className={`dashboard-stat-card dashboard-stat-card-${item.tone}`} bordered>
            <div className="dashboard-stat-head">
              <span className="dashboard-stat-icon">{item.icon}</span>
              <Tag bordered={false}>{item.trend}</Tag>
            </div>
            <div className="dashboard-stat-value">
              <strong>{item.value}</strong>
              <span>{item.unit}</span>
            </div>
            <Typography.Text type="secondary">{item.title}</Typography.Text>
          </ProCard>
        ))}
      </section>
      <Row gutter={[16, 16]}>
        <Col xs={24} xl={17}>
          <ProCard
            className="dashboard-table-card"
            title="履约待办"
            extra={
              <Space size={6}>
                <ClockCircleOutlined />
                <Typography.Text type="secondary">按交期和风险排序</Typography.Text>
              </Space>
            }
            bordered
          >
            <Table
              rowKey="id"
              dataSource={fulfillmentRows}
              pagination={false}
              size="middle"
              scroll={{ x: 660 }}
              columns={[
                { title: '订单号', dataIndex: 'id', width: 126 },
                { title: '客户', dataIndex: 'customer', width: 82 },
                { title: '款号', dataIndex: 'styleCode', width: 118 },
                { title: '数量', dataIndex: 'quantity', width: 68 },
                { title: '交期', dataIndex: 'deliveryDate', width: 96 },
                {
                  title: '状态',
                  dataIndex: 'status',
                  width: 78,
                  render: (status) => <OrderStatusTag status={String(status)} />,
                },
                { title: '下一步', dataIndex: 'nextAction', width: 92 },
              ]}
            />
          </ProCard>
        </Col>
        <Col xs={24} xl={7}>
          <ProCard className="dashboard-focus-card" title="今日重点" bordered>
            <div className="dashboard-focus-list">
              {focusItems.map((item) => (
                <div key={item.title} className={`dashboard-focus-item dashboard-focus-item-${item.tone}`}>
                  <div className="dashboard-focus-main">
                    <Space size={8} wrap>
                      <strong>{item.title}</strong>
                      <Tag bordered={false}>{item.tag}</Tag>
                    </Space>
                    <Typography.Text type="secondary">{item.subtitle}</Typography.Text>
                  </div>
                  <div className="dashboard-focus-meta">
                    <span>{item.owner}</span>
                    <span>{item.due}</span>
                  </div>
                </div>
              ))}
            </div>
          </ProCard>
          <ProCard className="dashboard-rhythm-card" title="产销节奏" bordered>
            <div className="dashboard-rhythm-list">
              {rhythmItems.map((item) => (
                <div key={item.label} className="dashboard-rhythm-item">
                  <div>
                    <strong>{item.label}</strong>
                    <Typography.Text type="secondary">{item.detail}</Typography.Text>
                  </div>
                  <Progress percent={item.value} showInfo={false} strokeColor="#2563eb" />
                </div>
              ))}
            </div>
            <div className="dashboard-rhythm-footer">
              <ArrowUpOutlined />
              <span>整体履约节奏较上周提升 6%</span>
            </div>
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
