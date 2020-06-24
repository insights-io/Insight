import ky from 'ky-universal';
import { DataResponse } from '@insight/types';

import { sessionApiBaseURL } from './base';

type SessionDTO = {
  id: string;
  deviceId: string;
  organizationId: string;
  ipAddress: string;
  userAgent: string;
  createdAt: string;
};

export type Session = Omit<SessionDTO, 'createdAt'> & {
  createdAt: Date;
};

const mapSession = (sessionDTO: SessionDTO): Session => {
  return { ...sessionDTO, createdAt: new Date(sessionDTO.createdAt) };
};

const SessionApi = {
  getSession: (sessionId: string) => {
    return ky
      .get(`${sessionApiBaseURL}/v1/sessions/${sessionId}`, {
        credentials: 'include',
      })
      .json<DataResponse<SessionDTO>>()
      .then((dataResponse) => mapSession(dataResponse.data));
  },
  getSessions: () => {
    return ky
      .get(`${sessionApiBaseURL}/v1/sessions?sort_by=-created_at`, {
        credentials: 'include',
      })
      .json<DataResponse<SessionDTO[]>>()
      .then((dataResponse) => dataResponse.data.map(mapSession));
  },
};

export default SessionApi;
