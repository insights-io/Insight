import type { ParsedUrlQuery } from 'querystring';

export const getQueryParam = (
  query: ParsedUrlQuery,
  key: string
): string | null => {
  const value = query[key];
  if (Array.isArray(value)) {
    return value[0];
  }
  return value ?? null;
};
