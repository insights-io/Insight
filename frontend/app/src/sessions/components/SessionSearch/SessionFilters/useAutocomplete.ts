import type { SessionSearchQueryParams } from '@rebrowse/sdk';
import { client } from 'sdk';
import { useQuery } from 'shared/hooks/useQuery';

export const cacheKey = (on: keyof SessionSearchQueryParams | undefined) => {
  return ['SessionApi', 'distinct', on];
};

const queryFn = (on: keyof SessionSearchQueryParams | undefined) => {
  return on === undefined
    ? []
    : client.recording.sessions
        .distinct(on)
        .then((httpResponse) => httpResponse.data);
};

export const useAutocomplete = (
  on: keyof SessionSearchQueryParams | undefined
) => {
  const { data: options = [] } = useQuery(cacheKey(on), () => queryFn(on));
  return { options };
};
