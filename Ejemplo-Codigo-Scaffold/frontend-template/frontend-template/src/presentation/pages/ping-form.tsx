import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { CURRENCIES, Money } from '../../domain';
import { messageForError } from '../i18n/error-messages';
import { useCreatePing } from '../hooks/use-pings';
import styles from './ping-page.module.css';

// Reglas SIMPLES del contrato (obligatoriedad, formato). Las de negocio (EX-0002/0003) las decide el backend.
export const pingFormSchema = z.object({
  message: z.string().trim().min(1, 'El mensaje es obligatorio.'),
  amount: z
    .string()
    .trim()
    .min(1, 'El importe es obligatorio.')
    .refine((v) => Money.isValidAmount(v), 'Importe inválido: use números con hasta 2 decimales (ej. 100.50).'),
  currency: z.enum(CURRENCIES),
});
export type PingFormValues = z.infer<typeof pingFormSchema>;

export const PingForm = () => {
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<PingFormValues>({
    resolver: zodResolver(pingFormSchema),
    defaultValues: { message: '', amount: '', currency: 'PEN' },
  });
  const create = useCreatePing();

  const onSubmit = handleSubmit((values) => {
    create.mutate(
      { message: values.message, amount: Money.parse(values.amount, values.currency) },
      { onSuccess: () => reset() },
    );
  });

  return (
    <form className={styles.form} onSubmit={onSubmit} noValidate aria-label="Nuevo ping">
      <div className={styles.field}>
        <label htmlFor="ping-message">Mensaje</label>
        <input id="ping-message" type="text" aria-invalid={!!errors.message} {...register('message')} />
        {errors.message && <p role="alert" className={styles.error}>{errors.message.message}</p>}
      </div>
      <div className={styles.field}>
        <label htmlFor="ping-amount">Importe</label>
        <input id="ping-amount" type="text" inputMode="decimal" aria-invalid={!!errors.amount} {...register('amount')} />
        {errors.amount && <p role="alert" className={styles.error}>{errors.amount.message}</p>}
      </div>
      <div className={styles.field}>
        <label htmlFor="ping-currency">Moneda</label>
        <select id="ping-currency" {...register('currency')}>
          {CURRENCIES.map((c) => (
            <option key={c} value={c}>{c}</option>
          ))}
        </select>
      </div>
      <button type="submit" disabled={create.isPending}>{create.isPending ? 'Enviando…' : 'Enviar ping'}</button>
      {create.isError && <p role="alert" className={styles.error}>{messageForError(create.error)}</p>}
      {create.isSuccess && <p role="status" className={styles.ok}>Ping registrado correctamente.</p>}
    </form>
  );
};
