import { stringify, ParsedUrlQueryInput } from 'querystring';

import type { Options } from 'ky';

export function withCredentials<T extends Options>(options: T): Options {
  return { ...options, credentials: 'include' };
}

export const querystring = (searchParams?: ParsedUrlQueryInput): string => {
  if (!searchParams) {
    return '';
  }
  if (Object.keys(searchParams).length === 0) {
    return '';
  }

  return `?${stringify(searchParams)}`;
};
