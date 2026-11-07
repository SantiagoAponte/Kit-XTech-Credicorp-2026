// Objeto de valor Money (ADR-006/A3 §6): el importe es un string decimal; NINGUNA operación usa number.
export const CURRENCIES = ['PEN', 'USD', 'EUR'] as const;
export type Currency = (typeof CURRENCIES)[number];

const AMOUNT_PATTERN = /^-?\d+(\.\d{1,2})?$/;

export class InvalidMoneyError extends Error {
  constructor(message: string) {
    super(message);
    this.name = 'InvalidMoneyError';
  }
}

export class Money {
  private constructor(
    readonly amount: string,
    readonly currency: Currency,
  ) {}

  /** Parsea un importe decimal como texto (hasta 2 decimales). Lanza InvalidMoneyError si es inválido. */
  static parse(amount: string, currency: string): Money {
    const trimmed = amount.trim();
    if (!AMOUNT_PATTERN.test(trimmed)) {
      throw new InvalidMoneyError(`Importe inválido: "${amount}"`);
    }
    if (!(CURRENCIES as readonly string[]).includes(currency)) {
      throw new InvalidMoneyError(`Moneda no soportada: "${currency}"`);
    }
    return new Money(Money.normalize(trimmed), currency as Currency);
  }

  static isValidAmount(amount: string): boolean {
    return AMOUNT_PATTERN.test(amount.trim());
  }

  /** Normaliza a exactamente 2 decimales y quita ceros a la izquierda ("5" -> "5.00", "007.5" -> "7.50"). */
  private static normalize(raw: string): string {
    const negative = raw.startsWith('-');
    const body = negative ? raw.slice(1) : raw;
    const [intPart = '0', frac = ''] = body.split('.');
    const int = intPart.replace(/^0+(?=\d)/, '');
    const decimals = frac.padEnd(2, '0');
    const isZero = /^0+$/.test(int) && /^0+$/.test(decimals);
    return `${negative && !isZero ? '-' : ''}${int}.${decimals}`;
  }

  /** Cierto si el importe es estrictamente mayor que cero (comparación textual). */
  isPositive(): boolean {
    return !this.amount.startsWith('-') && /[1-9]/.test(this.amount);
  }

  /** Formato de presentación con separador de miles, sin convertir a number: "PEN 1,234.50". */
  format(): string {
    const negative = this.amount.startsWith('-');
    const body = negative ? this.amount.slice(1) : this.amount;
    const [int = '0', frac = '00'] = body.split('.');
    const grouped = int.replace(/\B(?=(\d{3})+(?!\d))/g, ',');
    return `${this.currency} ${negative ? '-' : ''}${grouped}.${frac}`;
  }

  equals(other: Money): boolean {
    return this.amount === other.amount && this.currency === other.currency;
  }

  toJSON(): { amount: string; currency: Currency } {
    return { amount: this.amount, currency: this.currency };
  }
}
