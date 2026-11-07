import { ApplicationError } from '../../application';

/** Diccionario code -> mensaje en español (catálogo de errores del contrato). */
const MESSAGES: Record<string, string> = {
  'EX-0001': 'El formato de los datos enviados no es válido. Revise los campos.',
  'EX-0002': 'El mensaje no puede superar los 140 caracteres.',
  'EX-0003': 'El importe debe ser mayor que cero.',
  'EX-0004': 'No se encontró el ping solicitado.',
  'EX-0005': 'Falta la clave de idempotencia de la solicitud. Intente nuevamente.',
  NETWORK: 'No se pudo contactar con el servicio. Verifique su conexión e intente nuevamente.',
};

export const GENERIC_ERROR_MESSAGE = 'Ocurrió un error inesperado. Intente nuevamente más tarde.';

export const messageForCode = (code: string): string => MESSAGES[code] ?? GENERIC_ERROR_MESSAGE;

export const messageForError = (error: unknown): string =>
  error instanceof ApplicationError ? messageForCode(error.code) : GENERIC_ERROR_MESSAGE;
