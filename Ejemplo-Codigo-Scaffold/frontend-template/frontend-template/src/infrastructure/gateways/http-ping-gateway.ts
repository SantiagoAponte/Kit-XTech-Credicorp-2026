import {
  ApplicationError,
  NETWORK_ERROR_CODE,
  UNKNOWN_ERROR_CODE,
  type PingGateway,
} from '../../application';
import type { ApiClient } from '../api/client';
import { isProblem, moneyToDto, pingFromDto, pingPageFromDto } from '../mappers/ping-mapper';

/** Traduce Problem Details (o fallo de red) a ApplicationError. */
const toError = (error: unknown, status: number): ApplicationError =>
  isProblem(error)
    ? new ApplicationError(error.code, error.status ?? status, error.detail ?? error.title)
    : new ApplicationError(UNKNOWN_ERROR_CODE, status, 'Respuesta de error inesperada');

const guard = async <T>(call: () => Promise<T>): Promise<T> => {
  try {
    return await call();
  } catch (e) {
    if (e instanceof ApplicationError) throw e;
    throw new ApplicationError(NETWORK_ERROR_CODE, 0, 'No se pudo contactar con el servicio');
  }
};

export const createHttpPingGateway = (client: ApiClient): PingGateway => ({
  create: (newPing, idempotencyKey) =>
    guard(async () => {
      const { data, error, response } = await client.POST('/api/v1/pings', {
        params: { header: { 'Idempotency-Key': idempotencyKey } },
        body: { message: newPing.message, amount: moneyToDto(newPing.amount) },
      });
      if (data) return pingFromDto(data);
      throw toError(error, response.status);
    }),

  list: (page, size) =>
    guard(async () => {
      const { data, error, response } = await client.GET('/api/v1/pings', {
        params: { query: { page, size } },
      });
      if (data) return pingPageFromDto(data);
      throw toError(error, response.status);
    }),

  get: (id) =>
    guard(async () => {
      const { data, error, response } = await client.GET('/api/v1/pings/{id}', {
        params: { path: { id } },
      });
      if (data) return pingFromDto(data);
      throw toError(error, response.status);
    }),
});
