import { Navigate, createBrowserRouter } from 'react-router-dom';
import { AuthGuard } from '@/routes/AuthGuard';
import { DashboardLayout } from '@/layouts/DashboardLayout';
import { DashboardPage } from '@/pages/dashboard/DashboardPage';
import { LoginPage } from '@/pages/login/LoginPage';
import { CategoryManagementPage } from '@/pages/master/categories/CategoryManagementPage';
import { CustomerManagementPage } from '@/pages/master/customers/CustomerManagementPage';
import { MaterialManagementPage } from '@/pages/master/materials/MaterialManagementPage';
import { SupplierManagementPage } from '@/pages/master/suppliers/SupplierManagementPage';
import { SystemConfigPage } from '@/pages/system/configs/SystemConfigPage';
import { MenuManagementPage } from '@/pages/system/menus/MenuManagementPage';
import { RoleManagementPage } from '@/pages/system/roles/RoleManagementPage';
import { UserManagementPage } from '@/pages/system/users/UserManagementPage';

export const appRouter: ReturnType<typeof createBrowserRouter> = createBrowserRouter(
  [
    {
      path: '/login',
      element: <LoginPage />,
    },
    {
      path: '/',
      element: (
        <AuthGuard>
          <DashboardLayout />
        </AuthGuard>
      ),
      children: [
        {
          index: true,
          element: <DashboardPage />,
        },
        {
          path: 'system/users',
          element: <UserManagementPage />,
        },
        {
          path: 'system/roles',
          element: <RoleManagementPage />,
        },
        {
          path: 'system/menus',
          element: <MenuManagementPage />,
        },
        {
          path: 'system/configs',
          element: <SystemConfigPage />,
        },
        {
          path: 'master/materials',
          element: <MaterialManagementPage />,
        },
        {
          path: 'master/categories',
          element: <CategoryManagementPage />,
        },
        {
          path: 'master/customers',
          element: <CustomerManagementPage />,
        },
        {
          path: 'master/suppliers',
          element: <SupplierManagementPage />,
        },
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
