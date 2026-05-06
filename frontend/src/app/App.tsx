import { App as AntdApp } from 'antd';
import { RouterProvider } from 'react-router-dom';
import { appRouter } from '@/routes/appRouter';

export function App() {
  return (
    <AntdApp>
      <RouterProvider router={appRouter} />
    </AntdApp>
  );
}
