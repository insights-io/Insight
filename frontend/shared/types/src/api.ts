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

export enum SortDirection {
  ASC = '+',
  DESC = '-',
}

type SortByQueryParam<S extends string> = S | `${SortDirection}${S}`;

export type SearchBean<
  R extends Record<string, unknown>,
  GroupBy extends (keyof R)[] = []
> = {
  query?: string;
  limit?: number;
  sortBy?: SortByQueryParam<keyof R & string>[];
  groupBy?: GroupBy;
  dateTrunc?: TimePrecision;
} & Partial<Record<keyof R, QueryParam>>;

export type GroupByBaseResult = { count: number };

// eslint-disable-next-line @typescript-eslint/no-explicit-any
export type GroupByResult<T extends any[] = []> = T extends []
  ? GroupByBaseResult
  : (GroupByBaseResult & Record<T[number], string>)[];
