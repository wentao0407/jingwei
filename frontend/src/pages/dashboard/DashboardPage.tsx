import { CheckCircleOutlined, DatabaseOutlined, SafetyOutlined } from '@ant-design/icons';
import { ProCard, StatisticCard } from '@ant-design/pro-components';
import { Alert, Col, Row, Typography } from 'antd';

const overviewItems = [
  {
    title: '后端接口',
    value: 'Ready',
    icon: <DatabaseOutlined />,
  },
  {
    title: '鉴权状态',
    value: 'JWT',
    icon: <SafetyOutlined />,
  },
  {
    title: '前端骨架',
    value: 'React',
    icon: <CheckCircleOutlined />,
  },
];

export function DashboardPage() {
  return (
    <div className="dashboard-page">
      <Alert
        type="success"
        showIcon
        message="JingWei 前端工程已连接开发代理"
        description="API 请求会通过 /api 代理到本地后端服务。"
      />
      <Row gutter={[16, 16]}>
        {overviewItems.map((item) => (
          <Col key={item.title} xs={24} md={8}>
            <StatisticCard
              statistic={{
                title: item.title,
                value: item.value,
                icon: item.icon,
              }}
            />
          </Col>
        ))}
      </Row>
      <ProCard title="开发说明" bordered>
        <Typography.Paragraph>
          当前骨架包含登录、鉴权守卫、后台布局、API 客户端和统一错误处理，可在此基础上按业务模块扩展页面。
        </Typography.Paragraph>
      </ProCard>
    </div>
  );
}
