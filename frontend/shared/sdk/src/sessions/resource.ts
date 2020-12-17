import ky from 'ky-universal';
import type {
  DataResponse,
  SessionDTO,
  BrowserEventDTO,
} from '@rebrowse/types';

import { getData, querystring, withCredentials } from '../utils';
import type { RequestOptions } from '../types';

import type {
  SearchEventsRequestOptions,
  SessionsSearchRequestOptions,
} from './types';

export const createSessionsClient = (sessionApiBaseUrl: string) => {
  const SessionApi = {
    getSession: (
      sessionId: string,
      { baseURL = sessionApiBaseUrl, ...rest }: RequestOptions = {}
    ) => {
      return ky
        .get(`${baseURL}/v1/sessions/${sessionId}`, withCredentials(rest))
        .json<DataResponse<SessionDTO>>()
        .then(getData);
    },
    count: <T = { count: number }>({
      baseURL = sessionApiBaseUrl,
      search,
      ...rest
    }: SessionsSearchRequestOptions = {}) => {
      const searchQuery = querystring(search);
      return ky
        .get(
          `${baseURL}/v1/sessions/count${searchQuery}`,
          withCredentials(rest)
        )
        .json<DataResponse<T>>()
        .then(getData);
    },
    distinct: (
      on: string,
      { baseURL = sessionApiBaseUrl, ...rest }: RequestOptions = {}
    ) => {
      const searchQuery = querystring({ on });
      return ky
        .get(
          `${baseURL}/v1/sessions/distinct${searchQuery}`,
          withCredentials(rest)
        )
        .json<DataResponse<string[]>>()
        .then((dataResponse) => dataResponse.data);
    },

    getSessions: ({
      baseURL = sessionApiBaseUrl,
      search,
      ...rest
    }: SessionsSearchRequestOptions = {}) => {
      const searchQuery = querystring(search);
      return ky
        .get(`${baseURL}/v1/sessions${searchQuery}`, withCredentials(rest))
        .json<DataResponse<SessionDTO[]>>()
        .then(getData);
    },
  };

  const events = {
    search: (
      sessionId: string,
      {
        baseURL = sessionApiBaseUrl,
        search,
        ...rest
      }: SearchEventsRequestOptions = {}
    ) => {
      const query = decodeURIComponent(querystring(search));
      return ky
        .get(
          `${baseURL}/v1/sessions/${sessionId}/events/search${query}`,
          withCredentials(rest)
        )
        .json<DataResponse<BrowserEventDTO[]>>()
        .then(getData);
    },
  };

  return { ...SessionApi, events };
};
