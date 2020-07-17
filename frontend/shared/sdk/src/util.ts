/* eslint-disable lodash/prefer-lodash-typecheck */
import { stringify, ParsedUrlQueryInput } from 'querystring';

export type QueryParam =
  | string
  | number
  | boolean
  | string[]
  | number[]
  | boolean[]
  | null;

export const querystring = (searchParams?: ParsedUrlQueryInput): string => {
  if (!searchParams) {
    return '';
  }
  if (Object.keys(searchParams).length === 0) {
    return '';
  }

  return `?${stringify(searchParams)}`;
};
