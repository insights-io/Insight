import ky from 'ky-universal';
import type { DataResponse, SessionDTO, BrowserEventDTO } from '@insight/types';

import { querystring, withCredentials } from '../core/utils';
import type { RequestOptions } from '../core/types';

import type {
  SearchEventsRequestOptions,
  SessionsSearchRequestOptions,
} from './types';

export const createSessionsClient = (sessionApiBaseURL: string) => {
  function count<T = { count: number }>({
    baseURL = sessionApiBaseURL,
    search,
    ...rest
  }: SessionsSearchRequestOptions = {}) {
    const searchQuery = querystring(search);
    return ky
      .get(
        `${baseURL}/v1/sessions/insights/count${searchQuery}`,
        withCredentials(rest)
      )
      .json<DataResponse<T>>()
      .then((dataResponse) => dataResponse.data);
  }

  const SessionApi = {
    getSession: (
      sessionId: string,
      { baseURL = sessionApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      return ky
        .get(`${baseURL}/v1/sessions/${sessionId}`, withCredentials(rest))
        .json<DataResponse<SessionDTO>>()
        .then((dataResponse) => dataResponse.data);
    },
    count,
    distinct: (
      on: string,
      { baseURL = sessionApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      const searchQuery = querystring({ on });
      return ky
        .get(
          `${baseURL}/v1/sessions/insights/distinct${searchQuery}`,
          withCredentials(rest)
        )
        .json<DataResponse<(string | null)[]>>()
        .then(
          (dataResponse) =>
            dataResponse.data.filter((v) => v !== null) as string[]
        );
    },

    countByLocation: (params: RequestOptions = {}) => {
      return count<
        {
          count: number;
          'location.countryName': string;
          'location.continentName': string;
        }[]
      >({
        ...params,
        search: { group_by: ['location.countryName,location.continentName'] },
      });
    },
    countByDeviceClass: (options: RequestOptions = {}) => {
      return count<{ count: number; 'user_agent.deviceClass': string }[]>({
        ...options,
        search: { group_by: ['user_agent.deviceClass'] },
      }).then((dataResponse) => {
        return dataResponse.reduce((acc, entry) => {
          return { ...acc, [entry['user_agent.deviceClass']]: entry.count };
        }, {} as Record<string, number>);
      });
    },
    getSessions: ({
      baseURL = sessionApiBaseURL,
      search,
      ...rest
    }: SessionsSearchRequestOptions = {}) => {
      const searchQuery = querystring(search);

      return ky
        .get(`${baseURL}/v1/sessions${searchQuery}`, withCredentials(rest))
        .json<DataResponse<SessionDTO[]>>()
        .then((dataResponse) => dataResponse.data);
    },
  };

  const events = {
    search: (
      sessionId: string,
      {
        baseURL = sessionApiBaseURL,
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
        .then((dataResponse) => dataResponse.data);
    },
  };

  return { ...SessionApi, events };
};
