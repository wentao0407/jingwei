import { lazy, Suspense, type ReactElement } from 'react';
import { Navigate, createBrowserRouter } from 'react-router-dom';
import { LoadingState } from '@/components/state';
import { AuthGuard } from '@/routes/AuthGuard';
import { DashboardLayout } from '@/layouts/DashboardLayout';

const LoginPage = lazy(() => import('@/pages/login/LoginPage').then((module) => ({ default: module.LoginPage })));
const DashboardPage = lazy(() => import('@/pages/dashboard/DashboardPage').then((module) => ({ default: module.DashboardPage })));
const UserManagementPage = lazy(() => import('@/pages/system/users/UserManagementPage').then((module) => ({ default: module.UserManagementPage })));
const RoleManagementPage = lazy(() => import('@/pages/system/roles/RoleManagementPage').then((module) => ({ default: module.RoleManagementPage })));
const MenuManagementPage = lazy(() => import('@/pages/system/menus/MenuManagementPage').then((module) => ({ default: module.MenuManagementPage })));
const SystemConfigPage = lazy(() => import('@/pages/system/configs/SystemConfigPage').then((module) => ({ default: module.SystemConfigPage })));
const AuditLogPage = lazy(() => import('@/pages/system/audit-logs/AuditLogPage').then((module) => ({ default: module.AuditLogPage })));
const DataScopePage = lazy(() => import('@/pages/system/data-scopes/DataScopePage').then((module) => ({ default: module.DataScopePage })));
const MaterialManagementPage = lazy(() => import('@/pages/master/materials/MaterialManagementPage').then((module) => ({ default: module.MaterialManagementPage })));
const CategoryManagementPage = lazy(() => import('@/pages/master/categories/CategoryManagementPage').then((module) => ({ default: module.CategoryManagementPage })));
const SpuManagementPage = lazy(() => import('@/pages/master/spus/SpuManagementPage').then((module) => ({ default: module.SpuManagementPage })));
const SizeGroupManagementPage = lazy(() => import('@/pages/master/size-groups/SizeGroupManagementPage').then((module) => ({ default: module.SizeGroupManagementPage })));
const SeasonManagementPage = lazy(() => import('@/pages/master/seasons/SeasonManagementPage').then((module) => ({ default: module.SeasonManagementPage })));
const WarehouseManagementPage = lazy(() => import('@/pages/master/warehouses/WarehouseManagementPage').then((module) => ({ default: module.WarehouseManagementPage })));
const CodingRuleManagementPage = lazy(() => import('@/pages/master/coding-rules/CodingRuleManagementPage').then((module) => ({ default: module.CodingRuleManagementPage })));
const CustomerManagementPage = lazy(() => import('@/pages/master/customers/CustomerManagementPage').then((module) => ({ default: module.CustomerManagementPage })));
const SupplierManagementPage = lazy(() => import('@/pages/master/suppliers/SupplierManagementPage').then((module) => ({ default: module.SupplierManagementPage })));
const AttributeDefinitionPage = lazy(() => import('@/pages/master/attribute-defs/AttributeDefinitionPage').then((module) => ({ default: module.AttributeDefinitionPage })));
const SalesOrderListPage = lazy(() => import('@/pages/order/sales/SalesOrderListPage').then((module) => ({ default: module.SalesOrderListPage })));
const ProductionOrderListPage = lazy(() => import('@/pages/order/production/ProductionOrderListPage').then((module) => ({ default: module.ProductionOrderListPage })));
const ReturnOrderListPage = lazy(() => import('@/pages/order/returns/ReturnOrderListPage').then((module) => ({ default: module.ReturnOrderListPage })));
const ProcurementOrderListPage = lazy(() => import('@/pages/procurement/orders/ProcurementOrderListPage').then((module) => ({ default: module.ProcurementOrderListPage })));
const AsnManagementPage = lazy(() => import('@/pages/procurement/asns/AsnManagementPage').then((module) => ({ default: module.AsnManagementPage })));
const BomMrpPage = lazy(() => import('@/pages/procurement/bom-mrp/BomMrpPage').then((module) => ({ default: module.BomMrpPage })));
const ReceivingManagementPage = lazy(() => import('@/pages/procurement/receiving/ReceivingManagementPage').then((module) => ({ default: module.ReceivingManagementPage })));
const PutawayManagementPage = lazy(() => import('@/pages/procurement/putaway/PutawayManagementPage').then((module) => ({ default: module.PutawayManagementPage })));
const SupplierStatementPage = lazy(() => import('@/pages/procurement/statements/SupplierStatementPage').then((module) => ({ default: module.SupplierStatementPage })));
const InventoryStockPlaceholderPage = lazy(() => import('@/pages/inventory/stock/InventoryStockPlaceholderPage').then((module) => ({ default: module.InventoryStockPlaceholderPage })));
const InboundOrderPage = lazy(() => import('@/pages/inventory/inbound/InboundOrderPage').then((module) => ({ default: module.InboundOrderPage })));
const OutboundOrderPage = lazy(() => import('@/pages/inventory/outbound/OutboundOrderPage').then((module) => ({ default: module.OutboundOrderPage })));
const StocktakingPage = lazy(() => import('@/pages/inventory/stocktaking/StocktakingPage').then((module) => ({ default: module.StocktakingPage })));
const InventoryAlertPage = lazy(() => import('@/pages/inventory/alerts/InventoryAlertPage').then((module) => ({ default: module.InventoryAlertPage })));
const TransferOrderPage = lazy(() => import('@/pages/inventory/transfer/TransferOrderPage').then((module) => ({ default: module.TransferOrderPage })));
const MaterialIssuePage = lazy(() => import('@/pages/warehouse/material-issue/MaterialIssuePage').then((module) => ({ default: module.MaterialIssuePage })));
const MaterialReturnPage = lazy(() => import('@/pages/warehouse/material-return/MaterialReturnPage').then((module) => ({ default: module.MaterialReturnPage })));
const WavePickingPage = lazy(() => import('@/pages/warehouse/waves/WavePickingPage').then((module) => ({ default: module.WavePickingPage })));
const ShipmentPage = lazy(() => import('@/pages/warehouse/shipments/ShipmentPage').then((module) => ({ default: module.ShipmentPage })));
const PrintPage = lazy(() => import('@/pages/warehouse/print/PrintPage').then((module) => ({ default: module.PrintPage })));
const ApprovalCenterPage = lazy(() => import('@/pages/approval/ApprovalCenterPage').then((module) => ({ default: module.ApprovalCenterPage })));
const ApprovalConfigPage = lazy(() => import('@/pages/approval/configs/ApprovalConfigPage').then((module) => ({ default: module.ApprovalConfigPage })));
const NotificationCenterPage = lazy(() => import('@/pages/notification/NotificationCenterPage').then((module) => ({ default: module.NotificationCenterPage })));
const ReportCenterPage = lazy(() => import('@/pages/report/ReportCenterPage').then((module) => ({ default: module.ReportCenterPage })));
const CostAccountingPage = lazy(() => import('@/pages/cost/CostAccountingPage').then((module) => ({ default: module.CostAccountingPage })));

export const appRouter: ReturnType<typeof createBrowserRouter> = createBrowserRouter(
  [
    {
      path: '/login',
      element: withLazyRoute(<LoginPage />),
    },
    {
      path: '/',
      element: (
        <AuthGuard>
          <DashboardLayout />
        </AuthGuard>
      ),
      children: [
        { index: true, element: withLazyRoute(<DashboardPage />) },
        { path: 'system/users', element: withLazyRoute(<UserManagementPage />) },
        { path: 'system/roles', element: withLazyRoute(<RoleManagementPage />) },
        { path: 'system/menus', element: withLazyRoute(<MenuManagementPage />) },
        { path: 'system/configs', element: withLazyRoute(<SystemConfigPage />) },
        { path: 'system/audit-logs', element: withLazyRoute(<AuditLogPage />) },
        { path: 'system/data-scopes', element: withLazyRoute(<DataScopePage />) },
        { path: 'master/materials', element: withLazyRoute(<MaterialManagementPage />) },
        { path: 'master/categories', element: withLazyRoute(<CategoryManagementPage />) },
        { path: 'master/spus', element: withLazyRoute(<SpuManagementPage />) },
        { path: 'master/size-groups', element: withLazyRoute(<SizeGroupManagementPage />) },
        { path: 'master/seasons', element: withLazyRoute(<SeasonManagementPage />) },
        { path: 'master/warehouses', element: withLazyRoute(<WarehouseManagementPage />) },
        { path: 'master/coding-rules', element: withLazyRoute(<CodingRuleManagementPage />) },
        { path: 'master/customers', element: withLazyRoute(<CustomerManagementPage />) },
        { path: 'master/suppliers', element: withLazyRoute(<SupplierManagementPage />) },
        { path: 'master/attribute-defs', element: withLazyRoute(<AttributeDefinitionPage />) },
        { path: 'order/sales', element: withLazyRoute(<SalesOrderListPage />) },
        { path: 'order/production', element: withLazyRoute(<ProductionOrderListPage />) },
        { path: 'order/returns', element: withLazyRoute(<ReturnOrderListPage />) },
        { path: 'procurement/orders', element: withLazyRoute(<ProcurementOrderListPage />) },
        { path: 'procurement/asns', element: withLazyRoute(<AsnManagementPage />) },
        { path: 'procurement/bom-mrp', element: withLazyRoute(<BomMrpPage />) },
        { path: 'procurement/receiving', element: withLazyRoute(<ReceivingManagementPage />) },
        { path: 'procurement/putaway', element: withLazyRoute(<PutawayManagementPage />) },
        { path: 'procurement/statements', element: withLazyRoute(<SupplierStatementPage />) },
        { path: 'inventory/skus', element: withLazyRoute(<InventoryStockPlaceholderPage inventoryType="SKU" />) },
        {
          path: 'inventory/materials',
          element: withLazyRoute(<InventoryStockPlaceholderPage inventoryType="MATERIAL" />),
        },
        { path: 'inventory/inbounds', element: withLazyRoute(<InboundOrderPage />) },
        { path: 'inventory/outbounds', element: withLazyRoute(<OutboundOrderPage />) },
        { path: 'inventory/stocktaking', element: withLazyRoute(<StocktakingPage />) },
        { path: 'inventory/alerts', element: withLazyRoute(<InventoryAlertPage />) },
        { path: 'inventory/transfer', element: withLazyRoute(<TransferOrderPage />) },
        { path: 'warehouse/material-issue', element: withLazyRoute(<MaterialIssuePage />) },
        { path: 'warehouse/material-return', element: withLazyRoute(<MaterialReturnPage />) },
        { path: 'warehouse/waves', element: withLazyRoute(<WavePickingPage />) },
        { path: 'warehouse/shipments', element: withLazyRoute(<ShipmentPage />) },
        { path: 'warehouse/print', element: withLazyRoute(<PrintPage />) },
        { path: 'approval/tasks', element: withLazyRoute(<ApprovalCenterPage />) },
        { path: 'approval/configs', element: withLazyRoute(<ApprovalConfigPage />) },
        { path: 'notification/list', element: withLazyRoute(<NotificationCenterPage />) },
        { path: 'notification/preference', element: withLazyRoute(<NotificationCenterPage />) },
        { path: 'report/ledger', element: withLazyRoute(<ReportCenterPage />) },
        { path: 'report/flow', element: withLazyRoute(<ReportCenterPage />) },
        { path: 'report/age', element: withLazyRoute(<ReportCenterPage />) },
        { path: 'report/turnover', element: withLazyRoute(<ReportCenterPage />) },
        { path: 'report/shortage', element: withLazyRoute(<ReportCenterPage />) },
        { path: 'cost/query', element: withLazyRoute(<CostAccountingPage />) },
        { path: 'cost/report', element: withLazyRoute(<CostAccountingPage />) },
      ],
    },
    {
      path: '*',
      element: <Navigate to="/" replace />,
    },
  ],
  {
    future: {
      v7_relativeSplatPath: true,
    },
  },
);

function withLazyRoute(element: ReactElement) {
  return <Suspense fallback={<LoadingState message="页面加载中" />}>{element}</Suspense>;
}
