import { Component, Suspense, lazy, useMemo, type ReactNode } from 'react';
import { useRoutes, type RouteObject } from 'react-router-dom';
import type { RemoteDefinition } from '../remotes/registry';

class RemoteErrorBoundary extends Component<{ label: string; children: ReactNode }, { failed: boolean }> {
  state = { failed: false };
  static getDerivedStateFromError() {
    return { failed: true };
  }
  render() {
    if (this.state.failed) return <RemoteUnavailable label={this.props.label} />;
    return this.props.children;
  }
}

export const RemoteUnavailable = ({ label }: { label: string }) => (
  <div role="alert">
    <h2>El módulo «{label}» no está disponible</h2>
    <p>
      No se pudo cargar el micro-frontend. Verifique que esté en ejecución (por defecto en
      http://localhost:5174) o que <code>VITE_REMOTE_EXAMPLE_URL</code> apunte a su remoteEntry, y recargue la página.
    </p>
  </div>
);

const RemoteRoutes = ({ routes }: { routes: RouteObject[] }) => useRoutes(routes);

/** Carga perezosa de un remoto con manejo de error (nunca pantalla en blanco). */
export const RemoteModule = ({ remote }: { remote: RemoteDefinition }) => {
  const Lazy = useMemo(
    () =>
      lazy(async () => {
        const routes = await remote.load();
        return { default: () => <RemoteRoutes routes={routes} /> };
      }),
    [remote],
  );
  return (
    <RemoteErrorBoundary label={remote.label}>
      <Suspense fallback={<p role="status">Cargando módulo «{remote.label}»…</p>}>
        <Lazy />
      </Suspense>
    </RemoteErrorBoundary>
  );
};
