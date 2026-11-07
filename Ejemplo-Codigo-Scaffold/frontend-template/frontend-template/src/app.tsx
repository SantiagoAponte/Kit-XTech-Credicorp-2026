// Expuesto como ./App (opcional): el micro-frontend completo con su propio router.
import { createMemoryRouter, RouterProvider } from 'react-router-dom';
import { routes } from './remote-entry';

export const App = () => <RouterProvider router={createMemoryRouter(routes)} />;
export default App;
