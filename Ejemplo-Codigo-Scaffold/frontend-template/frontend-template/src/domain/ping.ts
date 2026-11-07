import type { Money } from './money';

export interface Ping {
  readonly id: string;
  readonly message: string;
  readonly amount: Money;
  /** Comisión calculada por el backend (regla EX-R01); el frontend nunca la calcula. */
  readonly fee?: Money;
  readonly receivedAt: string;
}

export interface NewPing {
  readonly message: string;
  readonly amount: Money;
}

export interface PingPage {
  readonly items: readonly Ping[];
  readonly page: number;
  readonly size: number;
  readonly totalElements: number;
}
