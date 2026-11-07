// Gateway HTTP con fetch simulado: mapeo DTO -> dominio y Problem Details -> ApplicationError.
import { describe, expect, it } from 'vitest';
import { createApiClient } from '../../src/infrastructure/api/client';
import { createHttpPingGateway } from '../../src/infrastructure/gateways/http-ping-gateway';
import { Money } from '../../src/domain';

const json = (status: number, body: unknown, type = 'application/json') =>
  new Response(JSON.stringify(body), { status, headers: { 'content-type': type } });

const gw = (handler: (req: Request) => Response | Promise<Response>) =>
  createHttpPingGateway(createApiClient('http://x', async (req) => handler(req)));

describe('HttpPingGateway', () => {
  it('create envía Idempotency-Key y mapea la respuesta', async () => {
    let seen: Request | undefined;
    const g = gw((req) => {
      seen = req;
      return json(201, { id: '1', message: 'hi', amount: { amount: '1000.00', currency: 'PEN' }, fee: { amount: '5.00', currency: 'PEN' }, receivedAt: '2026-01-01T00:00:00Z' });
    });
    const p = await g.create({ message: 'hi', amount: Money.parse('1000', 'PEN') }, 'k-1');
    expect(seen?.headers.get('Idempotency-Key')).toBe('k-1');
    expect(seen?.url).toBe('http://x/api/v1/pings');
    expect(await seen?.clone().json()).toEqual({ message: 'hi', amount: { amount: '1000.00', currency: 'PEN' } });
    expect(p.fee?.format()).toBe('PEN 5.00');
  });

  it('traduce Problem Details a ApplicationError', async () => {
    const g = gw(() => json(422, { type: 't', title: 'x', status: 422, code: 'EX-0002', detail: 'largo' }, 'application/problem+json'));
    await expect(g.create({ message: 'x', amount: Money.parse('1', 'PEN') }, 'k')).rejects.toMatchObject({ code: 'EX-0002', status: 422 });
  });

  it('fallo de red -> code NETWORK', async () => {
    const g = gw(() => { throw new TypeError('fail'); });
    await expect(g.list(0, 5)).rejects.toMatchObject({ code: 'NETWORK' });
  });
});
