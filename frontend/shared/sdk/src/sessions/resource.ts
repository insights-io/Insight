import type { SessionDTO, GroupByResult } from '@rebrowse/types';
import type { ExtendedRequestOptions } from 'types';
import { querystring } from 'utils';

import { HttpClient, jsonDataResponse } from '../http';

import type {
  SessionsSearchRequestOptions,
  SessionSearchQueryParams,
} from './types';

export const sessionsResource = (
  client: HttpClient,
  sessionApiBaseUrl: string
) => {
  return {
    retrieve: (
      sessionId: string,
      { baseUrl = sessionApiBaseUrl, ...rest }: ExtendedRequestOptions = {}
    ) => {
      return jsonDataResponse<SessionDTO>(
        client.get(`${baseUrl}/v1/sessions/${sessionId}`, rest)
      );
    },
    count: <GroupBy extends (keyof SessionSearchQueryParams)[] = []>({
      baseUrl = sessionApiBaseUrl,
      search,
      ...rest
    }: SessionsSearchRequestOptions<GroupBy> = {}) => {
      const searchQuery = querystring(search);
      return jsonDataResponse<GroupByResult<GroupBy>>(
        client.get(`${baseUrl}/v1/sessions/count${searchQuery}`, rest)
      );
    },
    distinct: (
      on: keyof SessionSearchQueryParams,
      { baseUrl = sessionApiBaseUrl, ...rest }: ExtendedRequestOptions = {}
    ) => {
      const searchQuery = querystring({ on });
      return jsonDataResponse<string[]>(
        client.get(`${baseUrl}/v1/sessions/distinct${searchQuery}`, rest)
      );
    },
    list: <GroupBy extends (keyof SessionSearchQueryParams)[] = []>({
      baseUrl = sessionApiBaseUrl,
      search,
      ...rest
    }: SessionsSearchRequestOptions<GroupBy> = {}) => {
      const searchQuery = querystring(search);
      return jsonDataResponse<SessionDTO[]>(
        client.get(`${baseUrl}/v1/sessions${searchQuery}`, rest)
      );
    },
  };
};
