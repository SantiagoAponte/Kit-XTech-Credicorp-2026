// ÚNICO sitio que ensambla adaptadores (infrastructure) con casos de uso (application).
import { makePingUseCases, type PingUseCases } from '../application';
import { createApiClient } from '../infrastructure/api/client';
import { createHttpPingGateway } from '../infrastructure/gateways/http-ping-gateway';

export const composeUseCases = (baseUrl = ''): PingUseCases =>
  makePingUseCases(createHttpPingGateway(createApiClient(baseUrl)));
