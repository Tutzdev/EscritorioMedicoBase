import { ApiError } from "../api/client";

export function getErrorMessage(
  error: unknown,
  fallback = "Não foi possível salvar as alterações.",
): string {
  return error instanceof ApiError ? error.message : fallback;
}
