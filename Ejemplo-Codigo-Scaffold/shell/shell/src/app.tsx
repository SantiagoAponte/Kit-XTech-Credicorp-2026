import { Route, Routes } from 'react-router-dom';
import { Layout } from './layout/layout';
import { Home } from './pages/home';
import { RemoteModule } from './pages/remote-module';
import { remotes } from './remotes/registry';

export const App = () => (
  <Routes>
    <Route element={<Layout />}>
      <Route index element={<Home />} />
      {remotes.map((r) => (
        <Route key={r.path} path={`${r.path}/*`} element={<RemoteModule remote={r} />} />
      ))}
      <Route path="*" element={<p>Página no encontrada.</p>} />
    </Route>
  </Routes>
);
