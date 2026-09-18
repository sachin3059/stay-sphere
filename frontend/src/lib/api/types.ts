/** Matches backend com.staysphere.common.ApiResponse */
export type ApiResponse<T> = {
  success: boolean;
  status: number;
  message: string;
  data: T;
  timestamp?: string;
};

export type ApiErrorBody = {
  success?: boolean;
  status?: number;
  error?: string;
  message?: string;
  path?: string;
};

export class ApiError extends Error {
  readonly status: number;
  readonly body?: ApiErrorBody;

  constructor(message: string, status: number, body?: ApiErrorBody) {
    super(message);
    this.name = "ApiError";
    this.status = status;
    this.body = body;
  }
}
