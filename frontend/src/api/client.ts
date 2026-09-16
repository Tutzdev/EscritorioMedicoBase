import type {
  ApiErrorBody,
  LoginResponse,
  TokenResponse,
} from "../types/api";

const API_URL = import.meta.env.VITE_API_URL ?? "/api";
const ACCESS_TOKEN_KEY = "medical-office.access-token";
const REFRESH_TOKEN_KEY = "medical-office.refresh-token";

let refreshPromise: Promise<TokenResponse> | null = null;

export class ApiError extends Error {
  readonly status: number;
  readonly body: ApiErrorBody | null;

  constructor(status: number, body: ApiErrorBody | null) {
    super(body?.message ?? "Não foi possível concluir a solicitação.");
    this.name = "ApiError";
    this.status = status;
    this.body = body;
  }
}

export function storeTokens(tokens: TokenResponse): void {
  localStorage.setItem(ACCESS_TOKEN_KEY, tokens.accessToken);
  localStorage.setItem(REFRESH_TOKEN_KEY, tokens.refreshToken);
}

export function clearTokens(): void {
  localStorage.removeItem(ACCESS_TOKEN_KEY);
  localStorage.removeItem(REFRESH_TOKEN_KEY);
}

export function hasStoredSession(): boolean {
  return Boolean(localStorage.getItem(REFRESH_TOKEN_KEY));
}

export async function loginRequest(
  email: string,
  password: string,
): Promise<LoginResponse> {
  return apiRequest<LoginResponse>("/auth/login", {
    method: "POST",
    body: JSON.stringify({ email, password }),
    skipAuthentication: true,
  });
}

interface ApiRequestOptions extends RequestInit {
  skipAuthentication?: boolean;
  skipRefresh?: boolean;
}

export async function apiRequest<T>(
  path: string,
  options: ApiRequestOptions = {},
): Promise<T> {
  const headers = new Headers(options.headers);
  const accessToken = localStorage.getItem(ACCESS_TOKEN_KEY);

  if (options.body && !headers.has("Content-Type")) {
    headers.set("Content-Type", "application/json");
  }

  if (!options.skipAuthentication && accessToken) {
    headers.set("Authorization", `Bearer ${accessToken}`);
  }

  const response = await fetch(`${API_URL}${path}`, {
    ...options,
    headers,
  });

  if (
    response.status === 401 &&
    !options.skipAuthentication &&
    !options.skipRefresh
  ) {
    const refreshed = await refreshSession();

    if (refreshed) {
      return apiRequest<T>(path, { ...options, skipRefresh: true });
    }
  }

  if (!response.ok) {
    throw await createApiError(response);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return (await response.json()) as T;
}

export function toQueryString(
  values: Record<string, string | number | boolean | null | undefined>,
): string {
  const params = new URLSearchParams();

  Object.entries(values).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== "") {
      params.set(key, String(value));
    }
  });

  const query = params.toString();
  return query ? `?${query}` : "";
}

async function refreshSession(): Promise<boolean> {
  const refreshToken = localStorage.getItem(REFRESH_TOKEN_KEY);

  if (!refreshToken) {
    notifyUnauthorized();
    return false;
  }

  if (!refreshPromise) {
    refreshPromise = apiRequest<TokenResponse>("/auth/refresh", {
      method: "POST",
      body: JSON.stringify({ refreshToken }),
      skipAuthentication: true,
      skipRefresh: true,
    }).finally(() => {
      refreshPromise = null;
    });
  }

  try {
    const tokens = await refreshPromise;
    storeTokens(tokens);
    return true;
  } catch {
    clearTokens();
    notifyUnauthorized();
    return false;
  }
}

async function createApiError(response: Response): Promise<ApiError> {
  try {
    const body = (await response.json()) as ApiErrorBody;
    return new ApiError(response.status, body);
  } catch {
    return new ApiError(response.status, null);
  }
}

function notifyUnauthorized(): void {
  window.dispatchEvent(new Event("medical-office:unauthorized"));
}
