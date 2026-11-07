import createClient from 'openapi-fetch';
import type { paths } from './schema';

/** Cliente tipado derivado del contrato. Rutas relativas (/api/...): proxy en dev, nginx en contenedor. */
export const createApiClient = (baseUrl = '', fetchImpl?: (req: Request) => Promise<Response>) =>
  createClient<paths>({ baseUrl, ...(fetchImpl ? { fetch: fetchImpl } : {}) });
export type ApiClient = ReturnType<typeof createApiClient>;
