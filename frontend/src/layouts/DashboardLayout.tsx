import {
  BellOutlined,
  DashboardOutlined,
  LogoutOutlined,
  ShopOutlined,
} from '@ant-design/icons';
import { PageContainer, ProLayout } from '@ant-design/pro-components';
import { Outlet, useNavigate } from 'react-router-dom';
import { Button, Space, Typography } from 'antd';
import { clearAccessToken } from '@/shared/storage/tokenStorage';

const menuItems = [
  {
    path: '/',
    name: '工作台',
    icon: <DashboardOutlined />,
  },
];

export function DashboardLayout() {
  const navigate = useNavigate();

  const handleLogout = () => {
    clearAccessToken();
    navigate('/login', { replace: true });
  };

  return (
    <ProLayout
      title="JingWei"
      logo={<ShopOutlined />}
      layout="mix"
      route={{
        path: '/',
        routes: menuItems,
      }}
      menuItemRender={(item, dom) => (
        <button className="layout-menu-button" onClick={() => navigate(item.path ?? '/')}>
          {dom}
        </button>
      )}
      actionsRender={() => [
        <Button key="notice" type="text" icon={<BellOutlined />} aria-label="通知" />,
        <Button
          key="logout"
          type="text"
          icon={<LogoutOutlined />}
          onClick={handleLogout}
          aria-label="退出登录"
        />,
      ]}
      avatarProps={{
        title: '经纬用户',
      }}
    >
      <PageContainer
        title="工作台"
        subTitle="服装生产销售全链路管理"
        extra={
          <Space>
            <Typography.Text type="secondary">开发环境</Typography.Text>
          </Space>
        }
      >
        <Outlet />
      </PageContainer>
    </ProLayout>
  );
}
