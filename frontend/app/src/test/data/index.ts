import { Session, SessionDTO, UserAgentDTO } from '@insight/types';
import { v4 as uuid } from 'uuid';
import { subSeconds, subDays } from 'date-fns';
import { mapSession } from '@insight/sdk';

import { INSIGHT_ADMIN } from './user';

export const DESKTOP_USER_AGENT: UserAgentDTO = {
  deviceClass: 'Desktop',
  operatingSystemName: 'Mac OS X',
  browserName: 'Chrome',
};

export const MOBILE_USER_AGENT: UserAgentDTO = {
  deviceClass: 'Phone',
  operatingSystemName: 'Android',
  browserName: 'Chrome',
};

export const INSIGHT_SESSION_DTO: SessionDTO = {
  id: uuid(),
  createdAt: new Date().toUTCString(),
  deviceId: '123',
  location: {
    ip: '82.192.62.51',
    city: 'Ljubljana',
    zip: '1000',
    latitude: 46.051429748535156,
    longitude: 14.505970001220703,
    countryName: 'Slovenia',
    regionName: 'Ljubljana',
  },
  organizationId: INSIGHT_ADMIN.organizationId,
  userAgent: DESKTOP_USER_AGENT,
};

export const INSIGHT_SESSION: Session = mapSession(INSIGHT_SESSION_DTO);

export const INSIGHT_SESSION_HOUR_AGO_DTO: SessionDTO = {
  id: uuid(),
  createdAt: subSeconds(new Date(), 3600).toUTCString(),
  deviceId: '123',
  location: {
    ip: '13.77.88.76',
    city: 'Boydton',
    zip: '23917',
    latitude: 36.667999267578125,
    longitude: -78.38899993896484,
    countryName: 'United States',
    regionName: 'Virginia',
  },
  organizationId: INSIGHT_ADMIN.organizationId,
  userAgent: DESKTOP_USER_AGENT,
};

export const INSIGHT_SESSION_HOUR_AGO: Session = mapSession(
  INSIGHT_SESSION_HOUR_AGO_DTO
);

export const INSIGHT_SESSION_DAY_AGO_NO_LOCATION_DTO: SessionDTO = {
  id: uuid(),
  createdAt: subDays(new Date(), 1).toUTCString(),
  deviceId: '123',
  location: { ip: '13.77.88.76' },
  organizationId: INSIGHT_ADMIN.organizationId,
  userAgent: MOBILE_USER_AGENT,
};

export const INSIGHT_SESSION_DAY_AGO_NO_LOCATION: Session = mapSession(
  INSIGHT_SESSION_DAY_AGO_NO_LOCATION_DTO
);

export const INSIGHT_SESSIONS_DTOS = [
  INSIGHT_SESSION_DTO,
  INSIGHT_SESSION_HOUR_AGO_DTO,
  INSIGHT_SESSION_DAY_AGO_NO_LOCATION_DTO,
];

export const INSIGHT_SESSIONS = [
  INSIGHT_SESSION,
  INSIGHT_SESSION_HOUR_AGO,
  INSIGHT_SESSION_DAY_AGO_NO_LOCATION,
];

export * from './events';
export * from './user';
