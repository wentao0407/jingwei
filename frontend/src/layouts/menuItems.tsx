import {
  AlertOutlined,
  ApartmentOutlined,
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
  MailOutlined,
  MenuOutlined,
  SendOutlined,
  SettingOutlined,
  ShopOutlined,
  ShoppingCartOutlined,
  SkinOutlined,
  SmileOutlined,
  SolutionOutlined,
  TeamOutlined,
  ToolOutlined,
  TruckOutlined,
  UserOutlined,
} from '@ant-design/icons';
import type { MenuDataItem } from '@ant-design/pro-components';
import type { AuthMenuItem } from '@/shared/storage/authSessionStorage';

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

export const fallbackMenuItems: MenuDataItem[] = [
  {
    key: 'dashboard',
    path: '/',
    name: '工作台',
    icon: <DashboardOutlined />,
  },
  {
    key: 'order',
    path: '/order',
    name: '订单管理',
    icon: <ShoppingCartOutlined />,
    children: [
      { key: 'order-sales', path: '/order/sales', name: '销售订单', icon: <FileTextOutlined /> },
      { key: 'order-production', path: '/order/production', name: '生产订单', icon: <ToolOutlined /> },
      { key: 'order-returns', path: '/order/returns', name: '退货单', icon: <CarryOutOutlined /> },
    ],
  },
  {
    key: 'procurement',
    path: '/procurement',
    name: '采购管理',
    icon: <TruckOutlined />,
    children: [
      { key: 'procurement-orders', path: '/procurement/orders', name: '采购订单', icon: <ShoppingCartOutlined /> },
      { key: 'procurement-asns', path: '/procurement/asns', name: '到货通知', icon: <TruckOutlined /> },
      { key: 'procurement-bom-mrp', path: '/procurement/bom-mrp', name: 'BOM与MRP', icon: <FileTextOutlined /> },
      { key: 'procurement-receiving', path: '/procurement/receiving', name: '收货管理', icon: <InboxOutlined /> },
      { key: 'procurement-putaway', path: '/procurement/putaway', name: '上架管理', icon: <ShopOutlined /> },
    ],
  },
  {
    key: 'inventory',
    path: '/inventory',
    name: '库存物流',
    icon: <DatabaseOutlined />,
    children: [
      { key: 'inventory-skus', path: '/inventory/skus', name: '库存 SKU', icon: <InboxOutlined /> },
      { key: 'inventory-materials', path: '/inventory/materials', name: '库存物料', icon: <DatabaseOutlined /> },
      { key: 'inventory-inbounds', path: '/inventory/inbounds', name: '入库单', icon: <InboxOutlined /> },
      { key: 'inventory-outbounds', path: '/inventory/outbounds', name: '出库单', icon: <TruckOutlined /> },
      { key: 'inventory-stocktaking', path: '/inventory/stocktaking', name: '盘点单', icon: <FileTextOutlined /> },
      { key: 'inventory-alerts', path: '/inventory/alerts', name: '库存预警', icon: <AlertOutlined /> },
      { key: 'warehouse-waves', path: '/warehouse/waves', name: '波次拣货', icon: <AppstoreOutlined /> },
      { key: 'warehouse-shipments', path: '/warehouse/shipments', name: '发运单', icon: <SendOutlined /> },
    ],
  },
  {
    key: 'approval',
    path: '/approval',
    name: '审批中心',
    icon: <AuditOutlined />,
    children: [
      { key: 'approval-tasks', path: '/approval/tasks', name: '我的审批', icon: <AuditOutlined /> },
      { key: 'approval-configs', path: '/approval/configs', name: '审批配置', icon: <SettingOutlined /> },
    ],
  },
  {
    key: 'notification',
    path: '/notification',
    name: '通知中心',
    icon: <BellOutlined />,
    children: [
      { key: 'notification-list', path: '/notification/list', name: '我的通知', icon: <MailOutlined /> },
      { key: 'notification-preference', path: '/notification/preference', name: '通知偏好', icon: <SettingOutlined /> },
    ],
  },
  {
    key: 'report',
    path: '/report',
    name: '报表中心',
    icon: <BarChartOutlined />,
    children: [
      { key: 'report-ledger', path: '/report/ledger', name: '库存台账', icon: <BookOutlined /> },
      { key: 'report-flow', path: '/report/flow', name: '出入库流水', icon: <FileTextOutlined /> },
      { key: 'report-age', path: '/report/age', name: '库龄分析', icon: <CalendarOutlined /> },
      { key: 'report-turnover', path: '/report/turnover', name: '畅滞销分析', icon: <BarChartOutlined /> },
    ],
  },
  {
    key: 'cost',
    path: '/cost',
    name: '成本核算',
    icon: <DollarOutlined />,
    children: [
      { key: 'cost-query', path: '/cost/query', name: '成本查询', icon: <DollarOutlined /> },
      { key: 'cost-report', path: '/cost/report', name: '成本报表', icon: <BarChartOutlined /> },
    ],
  },
  {
    key: 'master',
    path: '/master',
    name: '基础数据',
    icon: <DatabaseOutlined />,
    children: [
      { key: 'master-materials', path: '/master/materials', name: '物料管理', icon: <InboxOutlined /> },
      { key: 'master-categories', path: '/master/categories', name: '物料分类', icon: <ApartmentOutlined /> },
      { key: 'master-spus', path: '/master/spus', name: '款式管理', icon: <SkinOutlined /> },
      { key: 'master-size-groups', path: '/master/size-groups', name: '尺码组管理', icon: <ColumnWidthOutlined /> },
      { key: 'master-seasons', path: '/master/seasons', name: '季节波段', icon: <CalendarOutlined /> },
      { key: 'master-warehouses', path: '/master/warehouses', name: '仓库库位', icon: <ShopOutlined /> },
      { key: 'master-coding-rules', path: '/master/coding-rules', name: '编码规则', icon: <CodeOutlined /> },
      { key: 'master-suppliers', path: '/master/suppliers', name: '供应商管理', icon: <SolutionOutlined /> },
      { key: 'master-customers', path: '/master/customers', name: '客户管理', icon: <SmileOutlined /> },
    ],
  },
  {
    key: 'system',
    path: '/system',
    name: '系统管理',
    icon: <SettingOutlined />,
    children: [
      { key: 'system-users', path: '/system/users', name: '用户管理', icon: <UserOutlined /> },
      { key: 'system-roles', path: '/system/roles', name: '角色管理', icon: <TeamOutlined /> },
      { key: 'system-menus', path: '/system/menus', name: '菜单管理', icon: <MenuOutlined /> },
      { key: 'system-configs', path: '/system/configs', name: '系统配置', icon: <ToolOutlined /> },
      { key: 'system-audit-logs', path: '/system/audit-logs', name: '操作日志', icon: <FileTextOutlined /> },
      { key: 'system-data-scopes', path: '/system/data-scopes', name: '数据范围', icon: <ApartmentOutlined /> },
    ],
  },
];

export function buildMenuItems(menuTree: AuthMenuItem[]): MenuDataItem[] {
  return menuTree
    .filter(isVisibleMenu)
    .sort(compareMenu)
    .map((item) => ({
      key: item.id,
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

  if (path === '/system/audit-log') {
    return '/system/audit-logs';
  }

  if (path === '/system/data-scope') {
    return '/system/data-scopes';
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

  if (path === '/order/return') {
    return '/order/returns';
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
