import react from '@vitejs/plugin-react';
import { fileURLToPath, URL } from 'node:url';
import { defineConfig } from 'vitest/config';

interface PackageInfo {
  packageName: string;
}

function getPackageInfo(id: string): PackageInfo | null {
  const normalizedId = id.replace(/\\/g, '/');
  const nodeModulesIndex = normalizedId.lastIndexOf('/node_modules/');
  if (nodeModulesIndex === -1) {
    return null;
  }

  const packagePath = normalizedId.slice(nodeModulesIndex + '/node_modules/'.length);
  const segments = packagePath.split('/');
  if (segments[0].startsWith('@')) {
    return {
      packageName: `${segments[0]}/${segments[1]}`,
    };
  }

  return {
    packageName: segments[0],
  };
}

function isAntdEcosystem(packageName: string) {
  return (
    packageName === 'antd' ||
    packageName === '@ant-design/icons' ||
    packageName === '@ant-design/icons-svg' ||
    packageName === '@ant-design/cssinjs' ||
    packageName === '@ant-design/cssinjs-utils' ||
    packageName === '@ant-design/colors' ||
    packageName === '@ant-design/fast-color' ||
    packageName.startsWith('rc-') ||
    packageName.startsWith('@rc-component/') ||
    packageName === 'dayjs'
  );
}

export default defineConfig({
  plugins: [react()],
  build: {
    chunkSizeWarningLimit: 1500,
    rollupOptions: {
      output: {
        manualChunks(id) {
          const packageInfo = getPackageInfo(id);
          if (!packageInfo) {
            return undefined;
          }

          const { packageName } = packageInfo;
          if (
            packageName === 'react' ||
            packageName === 'react-dom' ||
            packageName === 'react-router' ||
            packageName === 'react-router-dom'
          ) {
            return 'vendor-misc';
          }

          if (packageName.startsWith('@ant-design/pro-')) {
            return 'vendor-pro';
          }

          if (isAntdEcosystem(packageName)) {
            return 'vendor-antd';
          }

          if (packageName === 'axios') {
            return 'vendor-http';
          }

          return 'vendor-misc';
        },
      },
    },
  },
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
  test: {
    environment: 'jsdom',
    globals: true,
    setupFiles: './src/test/setup.ts',
    testTimeout: 20000,
  },
});
