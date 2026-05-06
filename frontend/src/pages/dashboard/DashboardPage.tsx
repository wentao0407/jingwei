import { DownloadOutlined, PlusOutlined, SearchOutlined } from '@ant-design/icons';
import { ProCard, StatisticCard } from '@ant-design/pro-components';
import { Button, Input, Select, Space, Table, Tag } from 'antd';

const overviewItems = [
  {
    title: '今日新增',
    value: 12,
  },
  {
    title: '待审批',
    value: 18,
  },
  {
    title: '生产中',
    value: 64,
  },
  {
    title: '本周交付',
    value: 31,
  },
];

const orderRows = [
  {
    id: 'SO20260506001',
    customer: '杭州云织',
    styleCode: 'JW-POLO-2418',
    quantity: '3,200',
    deliveryDate: '2026-05-18',
    status: '待审批',
    owner: '陈敏',
  },
  {
    id: 'SO20260505012',
    customer: '上海锦棉',
    styleCode: 'JW-DRESS-1102',
    quantity: '1,860',
    deliveryDate: '2026-05-21',
    status: '生产中',
    owner: '王磊',
  },
  {
    id: 'SO20260504008',
    customer: '宁波海岚',
    styleCode: 'JW-COAT-0907',
    quantity: '920',
    deliveryDate: '2026-05-14',
    status: '待发运',
    owner: '刘佳',
  },
  {
    id: 'SO20260503019',
    customer: '苏州织造',
    styleCode: 'JW-SHIRT-3301',
    quantity: '4,500',
    deliveryDate: '2026-05-29',
    status: '生产中',
    owner: '周航',
  },
];

export function DashboardPage() {
  return (
    <div className="dashboard-page quiet-order-page">
      <section className="order-page-topbar">
        <div>
          <h1>销售订单</h1>
          <p>按客户、交期、状态跟踪订单履约进度</p>
        </div>
        <Space className="order-page-toolbar" wrap>
          <Input
            prefix={<SearchOutlined />}
            placeholder="搜索订单号/客户"
            className="order-search-input"
          />
          <Select
            defaultValue="all"
            options={[{ label: '全部状态', value: 'all' }]}
            className="order-status-select"
          />
          <Button icon={<DownloadOutlined />} aria-label="导出">
            导出
          </Button>
          <Button type="primary" icon={<PlusOutlined />} aria-label="新建订单">
            新建订单
          </Button>
        </Space>
      </section>
      <section className="order-stat-grid">
        {overviewItems.map((item) => (
          <StatisticCard key={item.title} statistic={item} />
        ))}
      </section>
      <ProCard className="order-table-card" bordered>
        <Table
          rowKey="id"
          dataSource={orderRows}
          pagination={false}
          columns={[
            {
              title: '订单号',
              dataIndex: 'id',
            },
            {
              title: '客户',
              dataIndex: 'customer',
            },
            {
              title: '款号',
              dataIndex: 'styleCode',
            },
            {
              title: '数量',
              dataIndex: 'quantity',
            },
            {
              title: '交期',
              dataIndex: 'deliveryDate',
            },
            {
              title: '状态',
              dataIndex: 'status',
              render: (status) => <OrderStatusTag status={String(status)} />,
            },
            {
              title: '负责人',
              dataIndex: 'owner',
            },
          ]}
        />
      </ProCard>
    </div>
  );
}

function OrderStatusTag({ status }: { status: string }) {
  const colorByStatus: Record<string, string> = {
    待审批: 'blue',
    生产中: 'green',
    待发运: 'gold',
  };

  return <Tag color={colorByStatus[status] ?? 'default'}>{status}</Tag>;
}
