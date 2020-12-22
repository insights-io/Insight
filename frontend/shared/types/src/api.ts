/* sortBy */

export enum SortDirection {
  ASC = '+',
  DESC = '-',
}

export type SortByQueryParam<S extends string> = S | `${SortDirection}${S}`;

/* groupBy */

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

export type CountResponse = { count: number };

export type WithCountResponse<
  TObject extends Record<string, string>
> = CountResponse & TObject;

export type GroupByResult<Fields extends string[] = []> = Fields extends []
  ? CountResponse
  : WithCountResponse<Record<Fields[number], string>>[];

/* search */

export type QueryParam =
  | string
  | number
  | boolean
  | string[]
  | number[]
  | boolean[]
  | null;

export type SearchBean<
  TObject extends Record<string, unknown>,
  GroupBy extends (keyof TObject)[] = []
> = {
  query?: string;
  limit?: number;
  sortBy?: SortByQueryParam<keyof TObject & string>[];
  groupBy?: GroupBy;
  dateTrunc?: TimePrecision;
} & Partial<Record<keyof TObject, QueryParam>>;

/* response */

export type DataResponse<Data> = {
  data: Data;
};

export type ApiErrors = {
  [name: string]: string | ApiErrors;
};

export type APIErrorDataResponse<Errors extends ApiErrors = ApiErrors> = {
  error: APIError<Errors>;
};

export type APIError<Errors extends ApiErrors = ApiErrors> = {
  statusCode: number;
  reason: string;
  message: string;
  errors?: Errors;
};
