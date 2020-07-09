import { UserDTO, Session, SessionDTO } from '@insight/types';
import { v4 as uuid } from 'uuid';
import { subSeconds } from 'date-fns';
import { mapSession } from '@insight/sdk';

export const USER_AGENT =
  'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.116 Safari/537.36';

export const INSIGHT_ADMIN: UserDTO = {
  id: '7c071176-d186-40ac-aaf8-ac9779ab047b',
  email: 'adming@insight.io',
  fullName: 'Admin Admin',
  organizationId: '000000',
  role: 'ADMIN',
  createdAt: new Date().toUTCString(),
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
