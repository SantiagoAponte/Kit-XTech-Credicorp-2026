// Lo expone la federación como ./routes. El shell lo monta bajo /example.
import type { RouteObject } from 'react-router-dom';
import { composeUseCases } from './bootstrap/composition-root';
import { createRoutes } from './presentation/routes';

export const routes: RouteObject[] = createRoutes(composeUseCases());
export default routes;
