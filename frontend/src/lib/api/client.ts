import { ApiError, type ApiErrorBody, type ApiResponse } from "./types";

const baseUrl = import.meta.env.VITE_API_URL ?? "http://localhost:8080";

export function getApiBaseUrl(): string {
  return baseUrl.replace(/\/$/, "");
}

type RequestOptions = {
  method?: string;
  body?: unknown;
  token?: string | null;
  idempotencyKey?: string;
  headers?: Record<string, string>;
};

export async function apiRequest<T>(
  path: string,
  options: RequestOptions = {},
): Promise<T> {
  const { method = "GET", body, token, idempotencyKey, headers = {} } =
    options;
  const url = `${getApiBaseUrl()}${path.startsWith("/") ? path : `/${path}`}`;

  const reqHeaders: Record<string, string> = {
    Accept: "application/json",
    ...headers,
  };

  if (body !== undefined) {
    reqHeaders["Content-Type"] = "application/json";
  }
  if (token) {
    reqHeaders.Authorization = `Bearer ${token}`;
  }
  if (idempotencyKey) {
    reqHeaders["Idempotency-Key"] = idempotencyKey;
  }

  const response = await fetch(url, {
    method,
    headers: reqHeaders,
    body: body !== undefined ? JSON.stringify(body) : undefined,
  });

  const text = await response.text();
  let json: unknown = null;
  if (text) {
    try {
      json = JSON.parse(text);
    } catch {
      /* non-json */
    }
  }

  if (!response.ok) {
    const errBody = json as ApiErrorBody | null;
    const message =
      errBody?.message ??
      (typeof json === "object" && json && "error" in json
        ? String((json as { error: string }).error)
        : response.statusText) ??
      "Request failed";
    throw new ApiError(message, response.status, errBody ?? undefined);
  }

  return json as T;
}

export async function apiData<T>(
  path: string,
  options: RequestOptions = {},
): Promise<T> {
  const wrapped = await apiRequest<ApiResponse<T>>(path, options);
  return wrapped.data;
}
