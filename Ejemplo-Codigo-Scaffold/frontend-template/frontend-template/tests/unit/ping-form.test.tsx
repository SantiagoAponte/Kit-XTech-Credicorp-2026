// Formulario: validación Zod y mapeo de EX-0002 a mensaje en español. Casos de uso falsos.
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ApplicationError, type PingUseCases } from '../../src/application';
import { PingForm } from '../../src/presentation/pages/ping-form';
import { UseCasesProvider } from '../../src/presentation/use-cases-context';
import { messageForCode, GENERIC_ERROR_MESSAGE } from '../../src/presentation/i18n/error-messages';

const setup = (createPing: PingUseCases['createPing']) => {
  const useCases: PingUseCases = { createPing, listPings: vi.fn(), getPing: vi.fn() };
  render(
    <QueryClientProvider client={new QueryClient({ defaultOptions: { mutations: { retry: false } } })}>
      <UseCasesProvider useCases={useCases}><PingForm /></UseCasesProvider>
    </QueryClientProvider>,
  );
  return userEvent.setup();
};

describe('PingForm', () => {
  it('valida con Zod: mensaje e importe obligatorios, importe con formato', async () => {
    const createPing = vi.fn();
    const user = setup(createPing);
    await user.click(screen.getByRole('button', { name: /enviar ping/i }));
    expect(await screen.findByText('El mensaje es obligatorio.')).toBeInTheDocument();
    expect(screen.getByText('El importe es obligatorio.')).toBeInTheDocument();
    await user.type(screen.getByLabelText('Mensaje'), 'hola');
    await user.type(screen.getByLabelText('Importe'), '1.234');
    await user.click(screen.getByRole('button', { name: /enviar ping/i }));
    expect(await screen.findByText(/Importe inválido/)).toBeInTheDocument();
    expect(createPing).not.toHaveBeenCalled();
  });

  it('envía Money con importe string al caso de uso', async () => {
    const createPing = vi.fn<PingUseCases['createPing']>(async () => ({}) as never);
    const user = setup(createPing);
    await user.type(screen.getByLabelText('Mensaje'), 'hola');
    await user.type(screen.getByLabelText('Importe'), '100.5');
    await user.selectOptions(screen.getByLabelText('Moneda'), 'USD');
    await user.click(screen.getByRole('button', { name: /enviar ping/i }));
    await waitFor(() => expect(createPing).toHaveBeenCalled());
    const arg = createPing.mock.calls[0]![0];
    expect(arg.message).toBe('hola');
    expect(arg.amount.toJSON()).toEqual({ amount: '100.50', currency: 'USD' });
    expect(await screen.findByText('Ping registrado correctamente.')).toBeInTheDocument();
  });

  it('mapea EX-0002 (422) a mensaje en español', async () => {
    const createPing = vi.fn(async () => { throw new ApplicationError('EX-0002', 422, 'detalle técnico'); });
    const user = setup(createPing);
    await user.type(screen.getByLabelText('Mensaje'), 'x'.repeat(141));
    await user.type(screen.getByLabelText('Importe'), '10');
    await user.click(screen.getByRole('button', { name: /enviar ping/i }));
    expect(await screen.findByRole('alert')).toHaveTextContent('El mensaje no puede superar los 140 caracteres.');
  });
});

describe('mapeo de códigos de error', () => {
  it('cubre EX-0001..EX-0005 y usa mensaje genérico para el resto', () => {
    for (const c of ['EX-0001', 'EX-0002', 'EX-0003', 'EX-0004', 'EX-0005']) {
      expect(messageForCode(c)).not.toBe(GENERIC_ERROR_MESSAGE);
    }
    expect(messageForCode('EX-9999')).toBe(GENERIC_ERROR_MESSAGE);
  });
});
