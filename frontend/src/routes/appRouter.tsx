import { Navigate, createBrowserRouter } from 'react-router-dom';
import { AuthGuard } from '@/routes/AuthGuard';
import { DashboardLayout } from '@/layouts/DashboardLayout';
import { DashboardPage } from '@/pages/dashboard/DashboardPage';
import { LoginPage } from '@/pages/login/LoginPage';
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
