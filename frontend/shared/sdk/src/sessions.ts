import ky from 'ky-universal';
import {
  DataResponse,
  SessionDTO,
  Session,
  BrowserEventDTO,
} from '@insight/types';

import { querystring, QueryParam } from './util';
import { RequestOptions } from './types';

export const mapSession = (sessionDTO: SessionDTO | Session): Session => {
  return { ...sessionDTO, createdAt: new Date(sessionDTO.createdAt) };
};

export const createSessionsClient = (sessionApiBaseURL: string) => {
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
    getSessions: ({
      baseURL = sessionApiBaseURL,
      ...rest
    }: RequestOptions = {}) => {
      return ky
        .get(`${baseURL}/v1/sessions?sort_by=-created_at`, {
          credentials: 'include',
          ...rest,
        })
        .json<DataResponse<SessionDTO[]>>()
        .then((dataResponse) => dataResponse.data);
    },
  };

  type SearchEventsRequestOptions = Omit<RequestOptions, 'searchParams'> & {
    searchParams?: {
      'event.e'?: QueryParam;
      limit?: number;
    };
  };

  const events = {
    search: (
      sessionId: string,
      {
        baseURL = sessionApiBaseURL,
        searchParams,
        ...rest
      }: SearchEventsRequestOptions = {}
    ) => {
      const query = decodeURIComponent(querystring(searchParams));
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
