// Escenario: EX-R01/ADR-006 - Money como objeto de valor con importe string (sin number).
import { describe, expect, it } from 'vitest';
import { InvalidMoneyError, Money } from '../../src/domain';

describe('Money', () => {
  it('parsea y normaliza a 2 decimales sin usar number', () => {
    expect(Money.parse('5', 'PEN').amount).toBe('5.00');
    expect(Money.parse('007.5', 'USD').amount).toBe('7.50');
    expect(Money.parse('-0.00', 'EUR').amount).toBe('0.00');
    expect(Money.parse('1000.00', 'PEN').toJSON()).toEqual({ amount: '1000.00', currency: 'PEN' });
  });

  it('conserva precisión que number perdería', () => {
    const big = '12345678901234567890.12';
    expect(Money.parse(big, 'PEN').amount).toBe(big);
    expect(Money.parse(big, 'PEN').format()).toBe('PEN 12,345,678,901,234,567,890.12');
  });

  it('formatea con separador de miles', () => {
    expect(Money.parse('1234.5', 'USD').format()).toBe('USD 1,234.50');
    expect(Money.parse('-999.99', 'EUR').format()).toBe('EUR -999.99');
    expect(Money.parse('0.01', 'PEN').format()).toBe('PEN 0.01');
  });

  it('rechaza importes y monedas inválidas', () => {
    for (const bad of ['', 'abc', '1.234', '1,50', '1e3', '--1', '.5']) {
      expect(() => Money.parse(bad, 'PEN'), bad).toThrow(InvalidMoneyError);
    }
    expect(() => Money.parse('1.00', 'COP')).toThrow(InvalidMoneyError);
  });

  it('isPositive y equals son textuales', () => {
    expect(Money.parse('0.01', 'PEN').isPositive()).toBe(true);
    expect(Money.parse('0.00', 'PEN').isPositive()).toBe(false);
    expect(Money.parse('-1.00', 'PEN').isPositive()).toBe(false);
    expect(Money.parse('1', 'PEN').equals(Money.parse('1.00', 'PEN'))).toBe(true);
    expect(Money.parse('1', 'PEN').equals(Money.parse('1.00', 'USD'))).toBe(false);
  });
});
