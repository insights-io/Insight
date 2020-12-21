import get from 'lodash/get';
import type { GroupByBaseResult, SearchBean } from '@rebrowse/types';

export const filterByParam = <
  TObject extends Record<string, unknown>,
  QueryKey extends keyof TObject,
  M,
  GroupBy extends (keyof TObject)[]
>(
  object: TObject,
  queryKey: QueryKey,
  search: SearchBean<never, GroupBy>,
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

export const countBy = <
  TObject extends Record<string, unknown>,
  TQueryParams extends Record<string, unknown>,
  GroupBy extends (keyof TQueryParams)[]
>(
  data: TObject[],
  filter: (r: TObject) => boolean,
  search: SearchBean<TQueryParams, GroupBy> | undefined,
  get: <TObjectKey extends keyof TObject>(
    object: TObject,
    path: TObjectKey | [TObjectKey]
  ) => TObject[TObjectKey]
) => {
  const map = data.reduce((acc, point) => {
    if (!filter(point)) {
      return acc;
    }

    const key = search?.groupBy
      ?.map(
        (field) => `${field}=${get(point, field as keyof TObject) || 'Unknown'}`
      )
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
    }, {} as Record<GroupBy[number], string>);
    return [...acc, { ...data, count: map[key] }];
  }, [] as (GroupByBaseResult & Record<GroupBy[number], string>)[]);
};
