import { createContext, useContext, type ReactNode } from 'react';
import type { PingUseCases } from '../application';

const Ctx = createContext<PingUseCases | null>(null);

export const UseCasesProvider = ({ useCases, children }: { useCases: PingUseCases; children: ReactNode }) => (
  <Ctx.Provider value={useCases}>{children}</Ctx.Provider>
);

export const useUseCases = (): PingUseCases => {
  const v = useContext(Ctx);
  if (!v) throw new Error('UseCasesProvider ausente');
  return v;
};
