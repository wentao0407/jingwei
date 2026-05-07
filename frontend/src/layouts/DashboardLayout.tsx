import {
  BellOutlined,
  DashboardOutlined,
  InboxOutlined,
  LogoutOutlined,
  ShoppingCartOutlined,
  ShopOutlined,
  SettingOutlined,
  TeamOutlined,
  TruckOutlined,
  UserOutlined,
} from '@ant-design/icons';
import { PageContainer, ProLayout } from '@ant-design/pro-components';
import type { MenuDataItem } from '@ant-design/pro-components';
import { Outlet, useLocation, useNavigate } from 'react-router-dom';
import { Button, Space, Typography } from 'antd';
import { clearAuthSession, getAuthSession, type AuthMenuItem } from '@/shared/storage/authSessionStorage';
import { clearAccessToken } from '@/shared/storage/tokenStorage';

const fallbackMenuItems: MenuDataItem[] = [
  {
    path: '/',
    name: '工作台',
    icon: <DashboardOutlined />,
  },
  {
    path: '/',
    name: '销售订单',
    icon: <ShoppingCartOutlined />,
  },
  {
    path: '/',
    name: '生产订单',
    icon: <ShopOutlined />,
  },
  {
    path: '/',
    name: '库存管理',
    icon: <InboxOutlined />,
  },
  {
    path: '/',
    name: '物流发运',
    icon: <TruckOutlined />,
  },
  {
    path: '/system',
    name: '系统管理',
    icon: <SettingOutlined />,
    children: [
      {
        path: '/system/users',
        name: '用户管理',
        icon: <UserOutlined />,
      },
      {
        path: '/system/roles',
        name: '角色管理',
        icon: <TeamOutlined />,
      },
    ],
  },
];

const iconMap = {
  DashboardOutlined: <DashboardOutlined />,
  InboxOutlined: <InboxOutlined />,
  SettingOutlined: <SettingOutlined />,
  ShoppingCartOutlined: <ShoppingCartOutlined />,
  ShopOutlined: <ShopOutlined />,
  TeamOutlined: <TeamOutlined />,
  TruckOutlined: <TruckOutlined />,
  UserOutlined: <UserOutlined />,
};

export function DashboardLayout() {
  const navigate = useNavigate();
  const location = useLocation();
  const authSession = getAuthSession();
  const menuItems = buildMenuItems(authSession?.menuTree ?? []);
  const pageMeta = getPageMeta(location.pathname);

  const handleLogout = () => {
    clearAccessToken();
    clearAuthSession();
    navigate('/login', { replace: true });
  };

  return (
    <ProLayout
      className="quiet-dashboard-layout"
      title="经纬"
      logo={<ShopOutlined />}
      layout="side"
      route={{
        path: '/',
        routes: menuItems.length > 0 ? menuItems : fallbackMenuItems,
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
        title: authSession?.realName || authSession?.username || '经纬用户',
      }}
    >
      <PageContainer
        title={pageMeta.title}
        subTitle={pageMeta.subTitle}
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

function getPageMeta(pathname: string) {
  if (pathname === '/system/users') {
    return {
      title: '用户管理',
      subTitle: '维护账号、状态和角色授权入口',
    };
  }

  if (pathname === '/system/roles') {
    return {
      title: '角色管理',
      subTitle: '维护角色、状态和权限配置入口',
    };
  }

  return {
    title: '销售订单',
    subTitle: '按客户、交期、状态跟踪订单履约进度',
  };
}

function buildMenuItems(menuTree: AuthMenuItem[]): MenuDataItem[] {
  return menuTree
    .filter(isVisibleMenu)
    .sort(compareMenu)
    .map((item) => ({
      path: normalizeMenuPath(item.path),
      name: item.name,
      icon: getMenuIcon(item.icon),
      children: buildMenuItems(item.children ?? []),
    }));
}

function normalizeMenuPath(path?: string | null): string {
  if (path === '/system/user') {
    return '/system/users';
  }

  if (path === '/system/role') {
    return '/system/roles';
  }

  return path || '/';
}

function isVisibleMenu(item: AuthMenuItem): boolean {
  return item.type !== 'BUTTON' && item.visible !== false && item.status !== 'INACTIVE';
}

function compareMenu(left: AuthMenuItem, right: AuthMenuItem): number {
  return (left.sortOrder ?? 0) - (right.sortOrder ?? 0);
}

function getMenuIcon(iconName?: string | null) {
  if (!iconName) {
    return undefined;
  }

  return iconMap[iconName as keyof typeof iconMap];
}
