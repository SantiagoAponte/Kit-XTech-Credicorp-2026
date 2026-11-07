import { useState } from 'react';
import { messageForError } from '../i18n/error-messages';
import { usePingList } from '../hooks/use-pings';
import styles from './ping-page.module.css';

const PAGE_SIZE = 5;

export const PingList = () => {
  const [page, setPage] = useState(0);
  const { data, isLoading, isError, error } = usePingList(page, PAGE_SIZE);

  if (isLoading) return <p role="status">Cargando…</p>;
  if (isError) return <p role="alert" className={styles.error}>{messageForError(error)}</p>;
  if (!data || data.items.length === 0) return <p>No hay pings registrados.</p>;

  const lastPage = Math.max(0, Math.ceil(data.totalElements / data.size) - 1);
  return (
    <section aria-label="Lista de pings">
      <table className={styles.table}>
        <thead>
          <tr><th>Mensaje</th><th>Importe</th><th>Comisión</th><th>Recibido</th></tr>
        </thead>
        <tbody>
          {data.items.map((p) => (
            <tr key={p.id}>
              <td>{p.message}</td>
              <td>{p.amount.format()}</td>
              <td>{p.fee ? p.fee.format() : '—'}</td>
              <td>{p.receivedAt}</td>
            </tr>
          ))}
        </tbody>
      </table>
      <nav className={styles.pager} aria-label="Paginación">
        <button type="button" onClick={() => setPage((p) => p - 1)} disabled={page <= 0}>Anterior</button>
        <span>Página {data.page + 1} de {lastPage + 1} ({data.totalElements} en total)</span>
        <button type="button" onClick={() => setPage((p) => p + 1)} disabled={page >= lastPage}>Siguiente</button>
      </nav>
    </section>
  );
};
