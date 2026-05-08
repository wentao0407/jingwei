import {
  ApartmentOutlined,
  BellOutlined,
  DashboardOutlined,
  DatabaseOutlined,
  HomeOutlined,
  InboxOutlined,
  LogoutOutlined,
  MenuOutlined,
  ShoppingCartOutlined,
  ShopOutlined,
  SmileOutlined,
  SolutionOutlined,
  SettingOutlined,
  TeamOutlined,
  ToolOutlined,
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
    path: '/master',
    name: '基础数据',
    icon: <DatabaseOutlined />,
    children: [
      {
        path: '/master/materials',
        name: '物料管理',
        icon: <InboxOutlined />,
      },
      {
        path: '/master/categories',
        name: '物料分类',
        icon: <ApartmentOutlined />,
      },
      {
        path: '/master/suppliers',
        name: '供应商管理',
        icon: <SolutionOutlined />,
      },
      {
        path: '/master/customers',
        name: '客户管理',
        icon: <SmileOutlined />,
      },
    ],
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
      {
        path: '/system/menus',
        name: '菜单管理',
        icon: <MenuOutlined />,
      },
      {
        path: '/system/configs',
        name: '系统配置',
        icon: <ToolOutlined />,
      },
    ],
  },
];

const iconMap = {
  ApartmentOutlined: <ApartmentOutlined />,
  BoxOutlined: <InboxOutlined />,
  DashboardOutlined: <DashboardOutlined />,
  DatabaseOutlined: <DatabaseOutlined />,
  HomeOutlined: <HomeOutlined />,
  InboxOutlined: <InboxOutlined />,
  MenuOutlined: <MenuOutlined />,
  SettingOutlined: <SettingOutlined />,
  ShoppingCartOutlined: <ShoppingCartOutlined />,
  ShopOutlined: <ShopOutlined />,
  SmileOutlined: <SmileOutlined />,
  SolutionOutlined: <SolutionOutlined />,
  TeamOutlined: <TeamOutlined />,
  ToolOutlined: <ToolOutlined />,
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

  if (pathname === '/system/menus') {
    return {
      title: '菜单管理',
      subTitle: '维护导航菜单、按钮权限点和前端路由配置',
    };
  }

  if (pathname === '/system/configs') {
    return {
      title: '系统配置',
      subTitle: '维护运行参数、密码策略和库存规则配置',
    };
  }

  if (pathname === '/master/materials') {
    return {
      title: '物料管理',
      subTitle: '维护面料、辅料和包装物料主数据',
    };
  }

  if (pathname === '/master/categories') {
    return {
      title: '物料分类',
      subTitle: '维护物料分类树和启停状态',
    };
  }

  if (pathname === '/master/customers') {
    return {
      title: '客户管理',
      subTitle: '维护客户档案、等级、结算方式和启停状态',
    };
  }

  if (pathname === '/master/suppliers') {
    return {
      title: '供应商管理',
      subTitle: '维护供应商档案、资质状态和交货周期',
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

  if (path === '/system/menu') {
    return '/system/menus';
  }

  if (path === '/system/config') {
    return '/system/configs';
  }

  if (path === '/master/customer') {
    return '/master/customers';
  }

  if (path === '/master/supplier') {
    return '/master/suppliers';
  }

  if (path === '/master/material') {
    return '/master/materials';
  }

  if (path === '/master/category') {
    return '/master/categories';
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
