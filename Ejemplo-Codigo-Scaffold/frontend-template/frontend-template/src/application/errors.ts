/** Error de aplicación con `code` estable del catálogo (Problem Details, RFC 9457). */
export class ApplicationError extends Error {
  constructor(
    readonly code: string,
    readonly status: number,
    message: string,
  ) {
    super(message);
    this.name = 'ApplicationError';
  }
}

export const NETWORK_ERROR_CODE = 'NETWORK';
export const UNKNOWN_ERROR_CODE = 'UNKNOWN';
