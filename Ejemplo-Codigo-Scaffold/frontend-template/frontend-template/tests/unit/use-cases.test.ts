// Casos de uso con gateway falso (sin DOM, sin HTTP).
import { describe, expect, it, vi } from 'vitest';
import { ApplicationError, makePingUseCases, type PingGateway } from '../../src/application';
import { Money, type Ping } from '../../src/domain';

const ping = (id: string): Ping => ({ id, message: 'hola', amount: Money.parse('10', 'PEN'), receivedAt: '2026-01-01T00:00:00Z' });

const fakeGateway = (): PingGateway => ({
  create: vi.fn(async () => ping('1')),
  list: vi.fn(async (page, size) => ({ items: [ping('1')], page, size, totalElements: 1 })),
  get: vi.fn(async (id) => {
    if (id === 'x') throw new ApplicationError('EX-0004', 404, 'no existe');
    return ping(id);
  }),
});

describe('casos de uso Ping', () => {
  it('createPing delega en el gateway con Idempotency-Key generada', async () => {
    const gw = fakeGateway();
    const { createPing } = makePingUseCases(gw);
    const input = { message: 'hola', amount: Money.parse('10', 'PEN') };
    await createPing(input);
    expect(gw.create).toHaveBeenCalledTimes(1);
    const key = vi.mocked(gw.create).mock.calls[0]![1];
    expect(key).toMatch(/^[0-9a-f-]{36}$/);
    await createPing(input, 'fija-1');
    expect(vi.mocked(gw.create).mock.calls[1]![1]).toBe('fija-1');
  });

  it('listPings aplica paginación por defecto', async () => {
    const gw = fakeGateway();
    const page = await makePingUseCases(gw).listPings();
    expect(gw.list).toHaveBeenCalledWith(0, 20);
    expect(page.totalElements).toBe(1);
  });

  it('getPing propaga el error con code del catálogo', async () => {
    const { getPing } = makePingUseCases(fakeGateway());
    await expect(getPing('x')).rejects.toMatchObject({ code: 'EX-0004', status: 404 });
    await expect(getPing('7')).resolves.toMatchObject({ id: '7' });
  });
});
