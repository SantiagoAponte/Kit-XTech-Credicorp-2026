import { defineConfig, loadEnv } from 'vite';
import react from '@vitejs/plugin-react';
import { federation } from '@module-federation/vite';

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '');
  const backend = env.BACKEND_URL ?? process.env.BACKEND_URL ?? 'http://localhost:8080';
  const proxy = { '/api': { target: backend, changeOrigin: true } };
  const cors = { 'Access-Control-Allow-Origin': '*' };
  return {
    plugins: [
      react(),
      federation({
        name: 'example',
        filename: 'remoteEntry.js',
        exposes: {
          './routes': './src/remote-entry.tsx',
          './App': './src/app.tsx',
        },
        shared: {
          react: { singleton: true, requiredVersion: '^19.0.0' },
          'react-dom': { singleton: true, requiredVersion: '^19.0.0' },
          'react-router-dom': { singleton: true, requiredVersion: '^7.0.0' },
        },
        dts: false,
      }),
    ],
    css: { modules: { localsConvention: 'camelCase' } },
    server: { port: 5174, strictPort: true, cors: true, headers: cors, proxy, origin: 'http://localhost:5174' },
    preview: { port: 5174, strictPort: true, cors: true, headers: cors, proxy },
    build: { target: 'esnext', minify: false },
    test: {
      environment: 'jsdom',
      globals: true,
      setupFiles: ['./tests/setup.ts'],
      include: ['tests/**/*.test.{ts,tsx}'],
      css: { modules: { classNameStrategy: 'non-scoped' } },
    },
  };
});
