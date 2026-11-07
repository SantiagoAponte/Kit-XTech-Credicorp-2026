import type { RouteObject } from 'react-router-dom';
import type { PingUseCases } from '../application';
import { PingModule } from './ping-module';

/** Rutas relativas: el shell las monta bajo /example. */
export const createRoutes = (useCases: PingUseCases): RouteObject[] => [
  { index: true, element: <PingModule useCases={useCases} /> },
];
