import { SessionSearchQueryParams } from '@rebrowse/sdk';
import { SessionApi } from 'api';
import { useQuery } from 'shared/hooks/useQuery';

export const cacheKey = (on: keyof SessionSearchQueryParams | undefined) => {
  return ['SessionApi', 'distinct', on];
};

const queryFn = (on: keyof SessionSearchQueryParams | undefined) => {
  return on === undefined ? [] : SessionApi.distinct(on);
};

export const useAutocomplete = (
  on: keyof SessionSearchQueryParams | undefined
) => {
  const { data: options = [] } = useQuery(cacheKey(on), () => queryFn(on));
  return { options };
};
