import type { NewPing, Ping, PingPage } from '../../domain';
import type { PingGateway } from '../ports/ping-gateway';

export type CreatePing = (input: NewPing, idempotencyKey?: string) => Promise<Ping>;
export type ListPings = (page?: number, size?: number) => Promise<PingPage>;
export type GetPing = (id: string) => Promise<Ping>;

export interface PingUseCases {
  createPing: CreatePing;
  listPings: ListPings;
  getPing: GetPing;
}

const newKey = (): string => globalThis.crypto.randomUUID();

export const makeCreatePing =
  (gateway: PingGateway): CreatePing =>
  (input, idempotencyKey = newKey()) =>
    gateway.create(input, idempotencyKey);

export const makeListPings =
  (gateway: PingGateway): ListPings =>
  (page = 0, size = 20) =>
    gateway.list(page, size);

export const makeGetPing =
  (gateway: PingGateway): GetPing =>
  (id) =>
    gateway.get(id);

export const makePingUseCases = (gateway: PingGateway): PingUseCases => ({
  createPing: makeCreatePing(gateway),
  listPings: makeListPings(gateway),
  getPing: makeGetPing(gateway),
});
