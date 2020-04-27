export const baseURL = 'http://localhost:8080';

export type DataResponse<T> = {
  data: T;
};

export type APIErrorDataResponse = {
  error: APIError;
};

export type APIError = {
  statusCode: number;
  reason: string;
  message: string;
};
