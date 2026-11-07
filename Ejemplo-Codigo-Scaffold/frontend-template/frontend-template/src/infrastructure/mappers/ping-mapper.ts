import { Money, type Ping, type PingPage } from '../../domain';
import type { components } from '../api/schema';

type PingDto = components['schemas']['Ping'];
type PingPageDto = components['schemas']['PingPage'];
type MoneyDto = components['schemas']['Money'];
type ProblemDto = components['schemas']['Problem'];

export const moneyFromDto = (dto: MoneyDto): Money => Money.parse(dto.amount, dto.currency);
export const moneyToDto = (money: Money): MoneyDto => ({ amount: money.amount, currency: money.currency });

export const pingFromDto = (dto: PingDto): Ping => ({
  id: dto.id,
  message: dto.message,
  amount: moneyFromDto(dto.amount),
  ...(dto.fee ? { fee: moneyFromDto(dto.fee) } : {}),
  receivedAt: dto.receivedAt,
});

export const pingPageFromDto = (dto: PingPageDto): PingPage => ({
  items: dto.items.map(pingFromDto),
  page: dto.page,
  size: dto.size,
  totalElements: dto.totalElements,
});

export const isProblem = (value: unknown): value is ProblemDto =>
  typeof value === 'object' && value !== null && typeof (value as ProblemDto).code === 'string';
