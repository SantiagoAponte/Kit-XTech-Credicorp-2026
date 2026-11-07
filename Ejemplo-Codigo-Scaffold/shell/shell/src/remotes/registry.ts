import type { RouteObject } from 'react-router-dom';

export interface RemoteDefinition {
  /** Nombre visible en la navegación. */
  label: string;
  /** Ruta base en la que se monta (sin barra final). */
  path: string;
  /** Carga perezosa de las rutas expuestas por el remoto (./routes). */
  load: () => Promise<RouteObject[]>;
}

// Para añadir otro micro-frontend: declare el remoto en vite.config.ts, su módulo en vite-env.d.ts y una entrada aquí.
export const remotes: RemoteDefinition[] = [
  {
    label: 'Ejemplo',
    path: '/example',
    load: async () => (await import('example/routes')).routes,
  },
];
