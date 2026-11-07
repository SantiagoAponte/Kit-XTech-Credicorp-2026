import type { NewPing, Ping, PingPage } from '../../domain';

/** Puerto de salida: lo implementa infrastructure. */
export interface PingGateway {
  create(newPing: NewPing, idempotencyKey: string): Promise<Ping>;
  list(page: number, size: number): Promise<PingPage>;
  get(id: string): Promise<Ping>;
}
