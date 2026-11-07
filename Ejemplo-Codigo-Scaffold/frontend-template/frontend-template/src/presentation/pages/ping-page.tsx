import { PingForm } from './ping-form';
import { PingList } from './ping-list';
import styles from './ping-page.module.css';

export const PingPage = () => (
  <div className={styles.root}>
    <h2>Ping (ejemplo del kit)</h2>
    <PingForm />
    <PingList />
  </div>
);
