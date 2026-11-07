// Shell: navegación, usuario SOLO DEV y estado de error del remoto no disponible.
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { afterEach, describe, expect, it, vi } from 'vitest';

const load = vi.fn();
vi.mock('../src/remotes/registry', () => ({
  remotes: [{ label: 'Ejemplo', path: '/example', load: () => load() }],
}));

import { App } from '../src/app';
import { ThemeProvider } from '../src/layout/theme';

const renderAt = (path: string) =>
  render(
    <ThemeProvider>
      <MemoryRouter initialEntries={[path]}><App /></MemoryRouter>
    </ThemeProvider>,
  );

afterEach(() => load.mockReset());

describe('Shell', () => {
  it('renderiza navegación y usuario de desarrollo marcado SOLO DEV', () => {
    renderAt('/');
    const nav = screen.getByRole('navigation', { name: 'Navegación principal' });
    expect(nav).toHaveTextContent('Inicio');
    expect(nav).toHaveTextContent('Ejemplo');
    expect(screen.getByTestId('dev-user')).toHaveTextContent('SOLO DEV');
  });

  it('muestra error claro si el remoto no está disponible (no pantalla en blanco)', async () => {
    load.mockRejectedValue(new Error('Failed to fetch remoteEntry'));
    vi.spyOn(console, 'error').mockImplementation(() => {});
    renderAt('/example');
    expect(await screen.findByRole('alert')).toHaveTextContent('no está disponible');
    expect(screen.getByRole('navigation', { name: 'Navegación principal' })).toBeInTheDocument();
  });

  it('monta las rutas del remoto cuando carga correctamente', async () => {
    load.mockResolvedValue([{ index: true, element: <p>Contenido del remoto</p> }]);
    renderAt('/example');
    expect(await screen.findByText('Contenido del remoto')).toBeInTheDocument();
  });
});
