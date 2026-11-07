import { defineConfig, loadEnv } from 'vite';
import react from '@vitejs/plugin-react';
import { federation } from '@module-federation/vite';

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '');
  const backend = env.BACKEND_URL ?? process.env.BACKEND_URL ?? 'http://localhost:8080';
  const remoteUrl =
    env.VITE_REMOTE_EXAMPLE_URL || process.env.VITE_REMOTE_EXAMPLE_URL || 'http://localhost:5174/remoteEntry.js';
  const proxy = { '/api': { target: backend, changeOrigin: true } };
  const isTest = !!process.env.VITEST;
  return {
    plugins: [
      react(),
      ...(isTest
        ? []
        : [
            federation({
              name: 'shell',
              remotes: {
                example: { type: 'module', name: 'example', entry: remoteUrl },
              },
              shared: {
                react: { singleton: true, requiredVersion: '^19.0.0' },
                'react-dom': { singleton: true, requiredVersion: '^19.0.0' },
                'react-router-dom': { singleton: true, requiredVersion: '^7.0.0' },
              },
              dts: false,
            }),
          ]),
    ],
    server: { port: 5173, strictPort: true, proxy },
    preview: { port: 5173, strictPort: true, proxy },
    build: { target: 'esnext' },
    test: {
      environment: 'jsdom',
      globals: true,
      setupFiles: ['./tests/setup.ts'],
      include: ['tests/**/*.test.{ts,tsx}'],
    },
  };
});
