import ky from 'ky-universal';
import {
  DataResponse,
  SessionDTO,
  Session,
  BrowserEventDTO,
} from '@insight/types';

import { RequestOptions } from './types';

export const mapSession = (sessionDTO: SessionDTO): Session => {
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
        .then((dataResponse) => mapSession(dataResponse.data));
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
        .then((dataResponse) => dataResponse.data.map(mapSession));
    },
  };

  const events = {
    get: (
      sessionId: string,
      { baseURL = sessionApiBaseURL, ...rest }: RequestOptions = {}
    ) => {
      return ky
        .get(
          `${baseURL}/v1/sessions/${sessionId}/events/search?limit=10000&event.e=eq:9`,
          { credentials: 'include', ...rest }
        )
        .json<DataResponse<BrowserEventDTO[]>>()
        .then((dataResponse) => dataResponse.data);
    },
  };

  return { ...SessionApi, events };
};
