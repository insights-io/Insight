import get from 'lodash/get';
import type { GroupByResult, SearchBean } from '@rebrowse/types';
import camelCase from 'lodash/camelCase';
import { isValid, parseISO } from 'date-fns';

const parseValue = (value: string | number | Date) => {
  if (!Number.isNaN(Number(value))) {
    return Number(value);
  }

  const maybeDate = parseISO(value as string);
  if (isValid(maybeDate)) {
    return maybeDate;
  }

  return value;
};

export const camelCaseField = (key: string) => {
  return key.split('.').map(camelCase).join('.');
};

export const getParsedValue = <T extends Record<string, unknown>>(
  value: T,
  key: string
) => parseValue(get(value, camelCaseField(key)) as string);

export const filterByParam = <
  TObject extends Record<string, unknown>,
  TSearchObject extends Record<string, unknown>,
  GroupBy extends (keyof TSearchObject & string)[] = []
>(
  value: TObject,
  search: SearchBean<TSearchObject, GroupBy> = {},
  { queryFn }: { queryFn?: (t: TObject, query: string) => boolean } = {}
): boolean => {
  // eslint-disable-next-line no-restricted-syntax
  for (const searchKey of Object.keys(search)) {
    const typedSearchKey = searchKey as keyof typeof search;
    if (
      typedSearchKey === 'groupBy' ||
      typedSearchKey === 'dateTrunc' ||
      typedSearchKey === 'limit' ||
      typedSearchKey === 'sortBy'
    ) {
      // eslint-disable-next-line no-continue
      continue;
    }

    if (typedSearchKey === 'query' && search.query && queryFn) {
      if (!queryFn(value, search.query)) {
        return false;
      }
    }

    const actualValue = getParsedValue(value, searchKey);
    const params = (Array.isArray(search[typedSearchKey])
      ? search[typedSearchKey]
      : [search[typedSearchKey]]) as string[];

    // eslint-disable-next-line no-restricted-syntax
    for (const queryParam of params) {
      const [termCondition, ...rest] = queryParam.split(':');
      const stringValue = rest.join(':');
      const expectedValue = parseValue(stringValue);

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
  }

  return true;
};

export const countBy = <
  TObject extends Record<string, unknown>,
  TQueryParams extends Record<string, unknown>,
  GroupBy extends (keyof TQueryParams & string)[] = []
>(
  data: TObject[],
  filter: (r: TObject) => boolean,
  search: SearchBean<TQueryParams, GroupBy> | undefined,
  get: <TObjectKey extends keyof TObject>(
    object: TObject,
    path: TObjectKey | [TObjectKey]
  ) => TObject[TObjectKey]
): GroupByResult<GroupBy> => {
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

  if (!search?.groupBy || search.groupBy.length === 0) {
    return {
      count: Object.values(map).reduce((acc, v) => acc + v, 0),
    } as GroupByResult<GroupBy>;
  }

  return Object.keys(map).reduce((acc, key) => {
    const fieldValuePairs = key.split('--');
    const data = fieldValuePairs.reduce((acc, pair) => {
      const [field, value] = pair.split('=');
      return { ...acc, [field]: value };
    }, {} as Record<GroupBy[number], string>);
    return [...acc, { ...data, count: map[key] }] as GroupByResult<GroupBy>;
  }, ([] as unknown) as GroupByResult<GroupBy>);
};
