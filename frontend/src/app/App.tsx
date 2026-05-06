import { App as AntdApp } from 'antd';
import { useEffect } from 'react';
import { RouterProvider } from 'react-router-dom';
import { appRouter } from '@/routes/appRouter';
import { onUnauthorized } from '@/shared/auth/authEvents';

const LOGIN_PATH = '/login';

export function App() {
  useEffect(() => {
    return onUnauthorized(() => {
      const currentPath = window.location.pathname;

      if (currentPath === LOGIN_PATH) {
        return;
      }

      void appRouter.navigate(LOGIN_PATH, {
        replace: true,
        state: {
          from: currentPath,
        },
      });
    });
  }, []);

  return (
    <AntdApp>
      <RouterProvider router={appRouter} />
    </AntdApp>
  );
}
