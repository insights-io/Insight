import get from 'lodash/get';
import type { SearchBean } from '@rebrowse/types';
import type { CountByFieldDataPoint } from 'modules/insights/pages/InsightsPage';

export const filterByParam = <
  TObject extends Record<string, unknown>,
  QueryKey extends keyof TObject,
  M
>(
  object: TObject,
  queryKey: QueryKey,
  search: SearchBean<never>,
  parseValue: (v: TObject[QueryKey]) => M,
  getValue: (
    object: TObject,
    path: QueryKey | [QueryKey]
  ) => TObject[QueryKey] = get
): boolean => {
  const actualValue = parseValue(getValue(object, queryKey));

  const params = (Array.isArray(search[queryKey])
    ? search[queryKey]
    : [search[queryKey]]) as string[];

  // eslint-disable-next-line no-restricted-syntax
  for (const queryParam of params) {
    const [termCondition, ...rest] = queryParam.split(':');
    const expectedValue = parseValue(rest.join(':') as TObject[QueryKey]);

    if (termCondition === 'gte' && actualValue < expectedValue) {
      return false;
    }
    if (termCondition === 'gt' && actualValue <= expectedValue) {
      return false;
    }
    if (termCondition === 'lte' && actualValue > expectedValue) {
      return false;
    }
    if (termCondition === 'lt' && actualValue >= expectedValue) {
      return false;
    }
    if (termCondition === 'eq' && actualValue !== expectedValue) {
      return false;
    }
  }

  return true;
};

export const countBy = <R extends Record<string, unknown>>(
  data: R[],
  filter: (r: R) => boolean,
  search: SearchBean<R> | undefined,
  get: <TKey extends keyof R>(object: R, path: TKey | [TKey]) => R[TKey]
) => {
  const map = data.reduce((acc, point) => {
    if (!filter(point)) {
      return acc;
    }

    const key = search?.groupBy
      ?.map((field) => `${field}=${get(point, field) || 'Unknown'}`)
      .join('--') as string;

    const value = acc[key];

    return {
      ...acc,
      [key]: (value || 0) + 1,
    };
  }, {} as Record<string, number>);

  return Object.keys(map).reduce((acc, key) => {
    const fieldValuePairs = key.split('--');

    const data = fieldValuePairs.reduce((acc, pair) => {
      const [field, value] = pair.split('=');
      return { ...acc, [field]: value };
    }, {} as Record<keyof R & string, R[keyof R]>);

    return [
      ...acc,
      { ...data, count: map[key] } as CountByFieldDataPoint<keyof R & string>,
    ];
  }, [] as CountByFieldDataPoint<keyof R & string>[]);
};
