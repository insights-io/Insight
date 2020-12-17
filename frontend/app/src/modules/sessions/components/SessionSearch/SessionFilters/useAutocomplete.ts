import { SessionApi } from 'api';
import { useQuery } from 'shared/hooks/useQuery';

export const cacheKey = (on: string | undefined) => {
  return ['SessionApi', 'distinct', on];
};

const queryFn = (on: string | undefined) => {
  return on === undefined ? [] : SessionApi.distinct(on);
};

export const useAutocomplete = (on: string | undefined) => {
  const { data: options = [] } = useQuery(cacheKey(on), () => queryFn(on));
  return { options };
};
