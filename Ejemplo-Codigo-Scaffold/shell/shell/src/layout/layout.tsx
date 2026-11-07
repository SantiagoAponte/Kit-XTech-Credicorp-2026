import { NavLink, Outlet } from 'react-router-dom';
import { remotes } from '../remotes/registry';
import { DEV_USER } from './dev-user';
import { useTheme } from './theme';
import styles from './layout.module.css';

export const Layout = () => {
  const { theme, toggle } = useTheme();
  return (
    <div className={styles.shell}>
      <header className={styles.header}>
        <strong className={styles.brand}>Taller de modernización</strong>
        <nav aria-label="Navegación principal" className={styles.nav}>
          <NavLink to="/" end>Inicio</NavLink>
          {remotes.map((r) => (
            <NavLink key={r.path} to={r.path}>{r.label}</NavLink>
          ))}
        </nav>
        <div className={styles.right}>
          <button type="button" onClick={toggle} aria-label="Cambiar tema">Tema: {theme === 'light' ? 'claro' : 'oscuro'}</button>
          <span className={styles.user} data-testid="dev-user">
            {DEV_USER.name} <span className={styles.badge}>SOLO DEV</span>
          </span>
        </div>
      </header>
      <main className={styles.main}><Outlet /></main>
    </div>
  );
};
