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

export enum TimePrecision {
  MICROSECONDS = 'microseconds',
  MILLISECONDS = 'milliseconds',
  SECOND = 'second',
  MINUTE = 'minute',
  HOUR = 'hour',
  DAY = 'day',
  WEEK = 'week',
  MONTH = 'month',
}

export type SearchBean = {
  query?: string;
  limit?: number;
  sortBy?: string[];
  groupBy?: string[];
  dateTrunc?: TimePrecision;
} & Record<string, QueryParam>;

export enum SortDirection {
  ASC = '+',
  DESC = '-',
}
