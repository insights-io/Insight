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
  errors?: Record<string, string>;
};

export type SearchBean = {
  limit?: number;
  // eslint-disable-next-line camelcase
  sort_by?: string[];
  // eslint-disable-next-line camelcase
  group_by?: string[];
};

export type QueryParam =
  | string
  | number
  | boolean
  | string[]
  | number[]
  | boolean[]
  | null;