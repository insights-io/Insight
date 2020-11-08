export type DataResponse<T> = {
  data: T;
};

export type ApiErrors = { [name: string]: string | ApiErrors };

export type APIErrorDataResponse<T extends ApiErrors = ApiErrors> = {
  error: APIError<T>;
};

export type APIError<T extends ApiErrors = ApiErrors> = {
  statusCode: number;
  reason: string;
  message: string;
  errors?: T;
};

export type QueryParam =
  | string
  | number
  | boolean
  | string[]
  | number[]
  | boolean[]
  | null;

export type SearchBean = {
  query?: string;
  limit?: number;
  // eslint-disable-next-line camelcase
  sort_by?: string[];
  // eslint-disable-next-line camelcase
  group_by?: string[];
} & Record<string, QueryParam>;
