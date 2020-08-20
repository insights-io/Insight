import ky from 'ky-universal';
import {
  DataResponse,
  SessionDTO,
  Session,
  BrowserEventDTO,
} from '@insight/types';

import { querystring, QueryParam } from './util';
import { RequestOptions, SearchBean } from './types';

export const mapSession = (sessionDTO: SessionDTO | Session): Session => {
  return { ...sessionDTO, createdAt: new Date(sessionDTO.createdAt) };
};

export type SessionSearchBean = SearchBean & {
  // eslint-disable-next-line camelcase
  created_at?: QueryParam;
  'location.countryName'?: QueryParam;
  'location.continentName'?: QueryParam;
  'location.city'?: QueryParam;
  'location.regionName'?: QueryParam;
  'location.ip'?: QueryParam;
};

export type SessionsSearchRequestOptions = Omit<
  RequestOptions,
  'searchParams'
> & {
  search?: SessionSearchBean;
};

export const createSessionsClient = (sessionApiBaseURL: string) => {
  function count<T = { count: number }>({
    baseURL = sessionApiBaseURL,
    search,
    ...rest
  }: SessionsSearchRequestOptions = {}) {
    const searchQuery = querystring(search);
    return ky
      .get(`${baseURL}/v1/sessions/insights/count${searchQuery}`, {
        credentials: 'include',
        ...rest,
      })
      .json<DataResponse<T>>()
      .then((dataResponse) => dataResponse.data);
  }

  const SessionApi = {
    getSession: (
      sessionId: string,
      { baseURL = sessionApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      return ky
        .get(`${baseURL}/v1/sessions/${sessionId}`, {
          credentials: 'include',
          ...rest,
        })
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
        .get(`${baseURL}/v1/sessions/insights/distinct${searchQuery}`, {
          credentials: 'include',
          ...rest,
        })
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
        search: {
          group_by: ['user_agent.deviceClass'],
        },
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
        .get(`${baseURL}/v1/sessions${searchQuery}`, {
          credentials: 'include',
          ...rest,
        })
        .json<DataResponse<SessionDTO[]>>()
        .then((dataResponse) => dataResponse.data);
    },
  };

  type SearchEventsRequestOptions = Omit<RequestOptions, 'searchParams'> & {
    search?: SearchBean & {
      'event.e'?: QueryParam;
    };
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
        .get(`${baseURL}/v1/sessions/${sessionId}/events/search${query}`, {
          credentials: 'include',
          ...rest,
        })
        .json<DataResponse<BrowserEventDTO[]>>()
        .then((dataResponse) => dataResponse.data);
    },
  };

  return { ...SessionApi, events };
};
