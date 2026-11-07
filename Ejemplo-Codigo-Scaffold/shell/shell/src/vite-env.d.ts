/// <reference types="vite/client" />
declare module '*.module.css' {
  const classes: Record<string, string>;
  export default classes;
}
// Módulos remotos federados (uno por micro-frontend). Añada aquí cada remoto nuevo.
declare module 'example/routes' {
  import type { RouteObject } from 'react-router-dom';
  export const routes: RouteObject[];
  export default routes;
}
