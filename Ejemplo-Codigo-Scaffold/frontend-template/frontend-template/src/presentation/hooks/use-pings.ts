import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import type { NewPing } from '../../domain';
import { useUseCases } from '../use-cases-context';

export const usePingList = (page: number, size: number) => {
  const { listPings } = useUseCases();
  return useQuery({ queryKey: ['pings', page, size], queryFn: () => listPings(page, size) });
};

export const useCreatePing = () => {
  const { createPing } = useUseCases();
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (input: NewPing) => createPing(input),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['pings'] }),
  });
};
