import ky from 'ky-universal';
import type { DataResponse, SessionDTO, GroupByResult } from '@rebrowse/types';

import { jsonResponse } from '../http';
import { querystring, withCredentials } from '../utils';
import type { RequestOptions } from '../types';

import { createEventsClient } from './events';
import { createPageVisitClient } from './page-visit';
import type {
  SessionsSearchRequestOptions,
  SessionSearchQueryParams,
} from './types';

export const createSessionsClient = (sessionApiBaseUrl: string) => {
  const SessionApi = {
    getSession: (
      sessionId: string,
      { baseURL = sessionApiBaseUrl, ...rest }: RequestOptions = {}
    ) => {
      return jsonResponse<DataResponse<SessionDTO>>(
        ky.get(`${baseURL}/v1/sessions/${sessionId}`, withCredentials(rest))
      );
    },
    count: <GroupBy extends (keyof SessionSearchQueryParams)[] = []>({
      baseURL = sessionApiBaseUrl,
      search,
      ...rest
    }: SessionsSearchRequestOptions<GroupBy> = {}) => {
      const searchQuery = querystring(search);
      return jsonResponse<DataResponse<GroupByResult<GroupBy>>>(
        ky.get(
          `${baseURL}/v1/sessions/count${searchQuery}`,
          withCredentials(rest)
        )
      );
    },
    distinct: (
      on: keyof SessionSearchQueryParams,
      { baseURL = sessionApiBaseUrl, ...rest }: RequestOptions = {}
    ) => {
      const searchQuery = querystring({ on });
      return jsonResponse<DataResponse<string[]>>(
        ky.get(
          `${baseURL}/v1/sessions/distinct${searchQuery}`,
          withCredentials(rest)
        )
      );
    },

    getSessions: <GroupBy extends (keyof SessionSearchQueryParams)[] = []>({
      baseURL = sessionApiBaseUrl,
      search,
      ...rest
    }: SessionsSearchRequestOptions<GroupBy> = {}) => {
      const searchQuery = querystring(search);
      return jsonResponse<DataResponse<SessionDTO[]>>(
        ky.get(`${baseURL}/v1/sessions${searchQuery}`, withCredentials(rest))
      );
    },
  };

  const events = createEventsClient(sessionApiBaseUrl);
  const pageVisit = createPageVisitClient(sessionApiBaseUrl);

  return { ...SessionApi, events, pageVisit };
};
