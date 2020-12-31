import { stringify } from 'querystring';

import type { Options } from 'ky';
import type { SearchBean } from '@rebrowse/types';

export function withCredentials<T extends Options>(options: T): Options {
  return { ...options, credentials: 'include' };
}

export const querystring = <
  P extends Record<string, unknown>,
  GroupBy extends (keyof P)[]
>(
  searchParams?: SearchBean<P, GroupBy>
): string => {
  if (!searchParams) {
    return '';
  }
  if (Object.keys(searchParams).length === 0) {
    return '';
  }

  return `?${stringify(searchParams)}`;
};

const rhsTermCondition = (key: string) => <T>(value: T): string => {
  // eslint-disable-next-line lodash/prefer-lodash-typecheck
  if (value instanceof Date) {
    return `${key}:${value.toISOString()}`;
  }
  return `${key}:${value}`;
};

export const TermCondition = {
  EQ: rhsTermCondition('eq'),
  GTE: rhsTermCondition('gte'),
  GT: rhsTermCondition('gt'),
  LTE: rhsTermCondition('lte'),
  LT: rhsTermCondition('lt'),
};

export type TermConditionKey = keyof typeof TermCondition;
