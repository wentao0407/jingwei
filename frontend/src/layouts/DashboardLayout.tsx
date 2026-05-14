import {
  ApartmentOutlined,
  AlertOutlined,
  AppstoreOutlined,
  AuditOutlined,
  BarChartOutlined,
  BellOutlined,
  BookOutlined,
  CalendarOutlined,
  CarryOutOutlined,
  CodeOutlined,
  ColumnWidthOutlined,
  DashboardOutlined,
  DatabaseOutlined,
  DollarOutlined,
  FileTextOutlined,
  HomeOutlined,
  InboxOutlined,
  LogoutOutlined,
  MailOutlined,
  MenuOutlined,
  ShoppingCartOutlined,
  ShopOutlined,
  SkinOutlined,
  SmileOutlined,
  SolutionOutlined,
  SettingOutlined,
  SendOutlined,
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
    path: '/order',
    name: '订单管理',
    icon: <ShoppingCartOutlined />,
    children: [
      {
        path: '/order/sales',
        name: '销售订单',
        icon: <FileTextOutlined />,
      },
      {
        path: '/order/production',
        name: '生产订单',
        icon: <ToolOutlined />,
      },
    ],
  },
  {
    path: '/procurement',
    name: '采购管理',
    icon: <TruckOutlined />,
    children: [
      {
        path: '/procurement/orders',
        name: '采购订单',
        icon: <ShoppingCartOutlined />,
      },
      {
        path: '/procurement/asns',
        name: '到货通知',
        icon: <TruckOutlined />,
      },
      {
        path: '/procurement/bom-mrp',
        name: 'BOM与MRP',
        icon: <FileTextOutlined />,
      },
      {
        path: '/procurement/receiving',
        name: '收货管理',
        icon: <InboxOutlined />,
      },
      {
        path: '/procurement/putaway',
        name: '上架管理',
        icon: <ShopOutlined />,
      },
    ],
  },
  {
    path: '/inventory',
    name: '库存物流',
    icon: <DatabaseOutlined />,
    children: [
      { path: '/inventory/skus', name: '库存 SKU', icon: <InboxOutlined /> },
      { path: '/inventory/materials', name: '库存物料', icon: <DatabaseOutlined /> },
      { path: '/inventory/inbounds', name: '入库单', icon: <InboxOutlined /> },
      { path: '/inventory/outbounds', name: '出库单', icon: <TruckOutlined /> },
      { path: '/inventory/stocktaking', name: '盘点单', icon: <FileTextOutlined /> },
      { path: '/inventory/alerts', name: '库存预警', icon: <AlertOutlined /> },
      { path: '/warehouse/waves', name: '波次拣货', icon: <AppstoreOutlined /> },
      { path: '/warehouse/shipments', name: '发运单', icon: <SendOutlined /> },
    ],
  },
  {
    path: '/approval',
    name: '审批中心',
    icon: <AuditOutlined />,
    children: [
      { path: '/approval/tasks', name: '我的审批', icon: <AuditOutlined /> },
    ],
  },
  {
    path: '/notification',
    name: '通知中心',
    icon: <BellOutlined />,
    children: [
      { path: '/notification/list', name: '我的通知', icon: <MailOutlined /> },
      { path: '/notification/preference', name: '通知偏好', icon: <SettingOutlined /> },
    ],
  },
  {
    path: '/report',
    name: '报表中心',
    icon: <BarChartOutlined />,
    children: [
      { path: '/report/ledger', name: '库存台账', icon: <BookOutlined /> },
      { path: '/report/flow', name: '出入库流水', icon: <FileTextOutlined /> },
      { path: '/report/age', name: '库龄分析', icon: <CalendarOutlined /> },
      { path: '/report/turnover', name: '畅滞销分析', icon: <BarChartOutlined /> },
    ],
  },
  {
    path: '/cost',
    name: '成本核算',
    icon: <DollarOutlined />,
    children: [
      { path: '/cost/query', name: '成本查询', icon: <DollarOutlined /> },
      { path: '/cost/report', name: '成本报表', icon: <BarChartOutlined /> },
    ],
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
        path: '/master/spus',
        name: '款式管理',
        icon: <SkinOutlined />,
      },
      {
        path: '/master/size-groups',
        name: '尺码组管理',
        icon: <ColumnWidthOutlined />,
      },
      {
        path: '/master/seasons',
        name: '季节波段',
        icon: <CalendarOutlined />,
      },
      {
        path: '/master/warehouses',
        name: '仓库库位',
        icon: <ShopOutlined />,
      },
      {
        path: '/master/coding-rules',
        name: '编码规则',
        icon: <CodeOutlined />,
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
  AlertOutlined: <AlertOutlined />,
  ApartmentOutlined: <ApartmentOutlined />,
  AppstoreOutlined: <AppstoreOutlined />,
  AuditOutlined: <AuditOutlined />,
  BarChartOutlined: <BarChartOutlined />,
  BookOutlined: <BookOutlined />,
  BoxOutlined: <InboxOutlined />,
  CalendarOutlined: <CalendarOutlined />,
  CarryOutOutlined: <CarryOutOutlined />,
  CodeOutlined: <CodeOutlined />,
  ColumnWidthOutlined: <ColumnWidthOutlined />,
  DashboardOutlined: <DashboardOutlined />,
  DatabaseOutlined: <DatabaseOutlined />,
  DollarOutlined: <DollarOutlined />,
  FileTextOutlined: <FileTextOutlined />,
  HomeOutlined: <HomeOutlined />,
  InboxOutlined: <InboxOutlined />,
  MailOutlined: <MailOutlined />,
  MenuOutlined: <MenuOutlined />,
  SettingOutlined: <SettingOutlined />,
  SendOutlined: <SendOutlined />,
  ShoppingCartOutlined: <ShoppingCartOutlined />,
  ShopOutlined: <ShopOutlined />,
  SkinOutlined: <SkinOutlined />,
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

  if (pathname === '/master/spus') {
    return {
      title: '款式管理',
      subTitle: '维护 SPU、颜色和 SKU 价格状态',
    };
  }

  if (pathname === '/master/size-groups') {
    return {
      title: '尺码组管理',
      subTitle: '维护尺码组和尺码明细',
    };
  }

  if (pathname === '/master/seasons') {
    return {
      title: '季节波段',
      subTitle: '维护季节周期、上市波段和关闭状态',
    };
  }

  if (pathname === '/master/warehouses') {
    return {
      title: '仓库库位',
      subTitle: '维护仓库档案、库位容量和冻结状态',
    };
  }

  if (pathname === '/master/coding-rules') {
    return {
      title: '编码规则',
      subTitle: '维护编码规则、规则段和流水号预览',
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

  if (pathname === '/order/sales') {
    return {
      title: '销售订单',
      subTitle: '按客户、交期、状态跟踪订单履约进度',
    };
  }

  if (pathname === '/order/production') {
    return {
      title: '生产订单',
      subTitle: '跟踪生产计划、工序状态、完工和入库进度',
    };
  }

  if (pathname === '/procurement/orders') {
    return {
      title: '采购订单',
      subTitle: '跟踪采购审批、下发、收货和完成状态',
    };
  }

  if (pathname === '/procurement/asns') {
    return {
      title: '到货通知',
      subTitle: '跟踪 ASN 到货、收货和质检状态',
    };
  }

  if (pathname === '/procurement/bom-mrp') {
    return {
      title: 'BOM 与 MRP',
      subTitle: '查看物料清单和 MRP 采购建议',
    };
  }

  if (pathname === '/procurement/receiving') {
    return {
      title: '收货管理',
      subTitle: '从 ASN 创建收货单并逐行确认实收数量',
    };
  }

  if (pathname === '/procurement/putaway') {
    return {
      title: '上架管理',
      subTitle: '推荐库位并确认收货行上架位置',
    };
  }

  if (pathname === '/inventory/skus') {
    return { title: '库存 SKU', subTitle: '查看成品库存数量、锁定数量和批次分布' };
  }

  if (pathname === '/inventory/materials') {
    return { title: '库存物料', subTitle: '查看面辅料库存数量、质检数量和批次分布' };
  }

  if (pathname === '/inventory/inbounds') {
    return { title: '入库单', subTitle: '查询入库单、查看明细并确认入库' };
  }

  if (pathname === '/inventory/outbounds') {
    return { title: '出库单', subTitle: '查询出库单、查看明细并确认出库' };
  }

  if (pathname === '/inventory/stocktaking') {
    return { title: '盘点单', subTitle: '查询盘点单、开始盘点并录入实盘数量' };
  }

  if (pathname === '/inventory/alerts') {
    return { title: '库存预警', subTitle: '扫描库存阈值、查询预警记录并确认处理' };
  }

  if (pathname === '/warehouse/waves') {
    return { title: '波次拣货', subTitle: '创建波次、确认拣货、完成拣货单和取消波次' };
  }

  if (pathname === '/warehouse/shipments') {
    return { title: '发运单', subTitle: '按出库单确认发运并关联销售订单' };
  }

  if (pathname === '/approval/tasks') {
    return { title: '审批中心', subTitle: '查看我的待审批任务并提交审批意见' };
  }

  if (pathname === '/notification/list' || pathname === '/notification/preference') {
    return { title: '通知中心', subTitle: '查看站内通知、标记已读并维护通知偏好' };
  }

  if (pathname.startsWith('/report')) {
    return { title: '报表中心', subTitle: '查询库存台账、出入库流水、库龄和畅滞销分析' };
  }

  if (pathname.startsWith('/cost')) {
    return { title: '成本核算', subTitle: '查询生产订单成本归集和领料成本明细' };
  }

  return {
    title: '工作台首页',
    subTitle: '集中查看订单、生产、库存、审批和发运待办',
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

  if (path === '/master/spu') {
    return '/master/spus';
  }

  if (path === '/master/sizeGroup' || path === '/master/size-group') {
    return '/master/size-groups';
  }

  if (path === '/master/season') {
    return '/master/seasons';
  }

  if (path === '/master/warehouse') {
    return '/master/warehouses';
  }

  if (path === '/master/codingRule' || path === '/master/coding-rule') {
    return '/master/coding-rules';
  }

  if (path === '/order/sale' || path === '/order/salesOrder') {
    return '/order/sales';
  }

  if (path === '/order/productionOrder' || path === '/order/production-order') {
    return '/order/production';
  }

  if (path === '/procurement/order' || path === '/procurement/procurement-order') {
    return '/procurement/orders';
  }

  if (path === '/procurement/asn') {
    return '/procurement/asns';
  }

  if (path === '/procurement/bom' || path === '/procurement/mrp') {
    return '/procurement/bom-mrp';
  }

  if (path === '/warehouse/receiving' || path === '/procurement/receive') {
    return '/procurement/receiving';
  }

  if (path === '/warehouse/putaway' || path === '/procurement/put-away') {
    return '/procurement/putaway';
  }

  if (path === '/inventory/sku') return '/inventory/skus';
  if (path === '/inventory/material') return '/inventory/materials';
  if (path === '/inventory/inbound') return '/inventory/inbounds';
  if (path === '/inventory/outbound') return '/inventory/outbounds';
  if (path === '/inventory/stocktaking-order') return '/inventory/stocktaking';
  if (path === '/inventory/alert') return '/inventory/alerts';
  if (path === '/warehouse/wave' || path === '/warehouse/pick') return '/warehouse/waves';
  if (path === '/warehouse/ship' || path === '/warehouse/shipment') return '/warehouse/shipments';
  if (path === '/approval' || path === '/approval/task') return '/approval/tasks';
  if (path === '/notification' || path === '/notification/message') return '/notification/list';
  if (path === '/notification/prefs' || path === '/notification/preferences') return '/notification/preference';
  if (path === '/report') return '/report/ledger';
  if (path === '/cost') return '/cost/query';

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
