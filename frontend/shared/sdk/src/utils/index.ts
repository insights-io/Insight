import { stringify } from 'querystring';

import type { Options } from 'ky';
import type { DataResponse, SearchBean } from '@rebrowse/types';

export function withCredentials<T extends Options>(options: T): Options {
  return { ...options, credentials: 'include' };
}

export const getData = <T>(dataResponse: DataResponse<T>) => {
  return dataResponse.data;
};

export const querystring = <P extends Record<string, unknown>>(
  searchParams?: SearchBean<P>
): string => {
  if (!searchParams) {
    return '';
  }
  if (Object.keys(searchParams).length === 0) {
    return '';
  }

  return `?${stringify(searchParams)}`;
};
