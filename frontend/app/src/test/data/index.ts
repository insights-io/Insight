import { Session, SessionDTO, UserAgentDTO } from '@insight/types';
import { v4 as uuid } from 'uuid';
import { subSeconds } from 'date-fns';
import { mapSession } from '@insight/sdk';

import { INSIGHT_ADMIN } from './user';

export const USER_AGENT: UserAgentDTO = {
  deviceClass: 'Desktop',
  operatingSystemName: 'Mac OS X',
  browserName: 'Chrome',
};

export const INSIGHT_SESSION_DTO: SessionDTO = {
  id: uuid(),
  createdAt: new Date().toUTCString(),
  deviceId: '123',
  ipAddress: '127.0.0.1',
  organizationId: INSIGHT_ADMIN.organizationId,
  userAgent: USER_AGENT,
};

export const INSIGHT_SESSION: Session = mapSession(INSIGHT_SESSION_DTO);

export const INSIGHT_SESSION_HOUR_AGO_DTO: SessionDTO = {
  id: uuid(),
  createdAt: subSeconds(new Date(), 3600).toUTCString(),
  deviceId: '123',
  ipAddress: '127.0.0.1',
  organizationId: INSIGHT_ADMIN.organizationId,
  userAgent: USER_AGENT,
};

export const INSIGHT_SESSION_HOUR_AGO: Session = mapSession(
  INSIGHT_SESSION_HOUR_AGO_DTO
);

export * from './events';
export * from './user';
