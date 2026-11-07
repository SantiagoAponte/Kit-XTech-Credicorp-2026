import { useState } from 'react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import type { PingUseCases } from '../application';
import { UseCasesProvider } from './use-cases-context';
import { PingPage } from './pages/ping-page';

/** Raíz del módulo: proveedores propios (cliente de consultas aislado del shell). */
export const PingModule = ({ useCases }: { useCases: PingUseCases }) => {
  const [client] = useState(() => new QueryClient({ defaultOptions: { queries: { retry: false } } }));
  return (
    <QueryClientProvider client={client}>
      <UseCasesProvider useCases={useCases}>
        <PingPage />
      </UseCasesProvider>
    </QueryClientProvider>
  );
};
