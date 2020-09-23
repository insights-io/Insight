import { stringify, ParsedUrlQueryInput } from 'querystring';

import type { Options } from 'ky';
import type { DataResponse } from '@insight/types';

export function withCredentials<T extends Options>(options: T): Options {
  return { ...options, credentials: 'include' };
}

export const getData = <T>(dataResponse: DataResponse<T>) => {
  return dataResponse.data;
};

export const querystring = (searchParams?: ParsedUrlQueryInput): string => {
  if (!searchParams) {
    return '';
  }
  if (Object.keys(searchParams).length === 0) {
    return '';
  }

  return `?${stringify(searchParams)}`;
};
