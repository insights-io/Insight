import { Session, SessionDTO, UserAgentDTO, TfaMethod } from '@rebrowse/types';
import { v4 as uuid } from 'uuid';
import { subSeconds, subDays, subMonths } from 'date-fns';
import { mapSession } from '@rebrowse/sdk';

import { REBROWSE_ADMIN_DTO } from './user';

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

export const REBROWSE_SESSION_DTO: SessionDTO = {
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
  organizationId: REBROWSE_ADMIN_DTO.organizationId,
  userAgent: DESKTOP_USER_AGENT,
};

export const REBROWSE_SESSION: Session = mapSession(REBROWSE_SESSION_DTO);

export const REBROWSE_SESSION_HOUR_AGO_DTO: SessionDTO = {
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
  organizationId: REBROWSE_ADMIN_DTO.organizationId,
  userAgent: DESKTOP_USER_AGENT,
};

export const REBROWSE_SESSION_HOUR_AGO: Session = mapSession(
  REBROWSE_SESSION_HOUR_AGO_DTO
);

export const REBROWSE_SESSION_DAY_AGO_NO_LOCATION_DTO: SessionDTO = {
  id: uuid(),
  createdAt: subDays(new Date(), 1).toUTCString(),
  deviceId: '123',
  location: { ip: '13.77.88.76' },
  organizationId: REBROWSE_ADMIN_DTO.organizationId,
  userAgent: MOBILE_USER_AGENT,
};

export const REBROWSE_SESSION_DAY_AGO_NO_LOCATION: Session = mapSession(
  REBROWSE_SESSION_DAY_AGO_NO_LOCATION_DTO
);

export const REBROWSE_SESSION_MONTH_AGO_NO_LOCATION_DTO: SessionDTO = {
  id: uuid(),
  createdAt: subMonths(new Date(), 1).toUTCString(),
  deviceId: '123',
  location: { ip: '13.77.88.76' },
  organizationId: REBROWSE_ADMIN_DTO.organizationId,
  userAgent: MOBILE_USER_AGENT,
};

export const REBROWSE_SESSION_MONTH_AGO_NO_LOCATION: Session = mapSession(
  REBROWSE_SESSION_MONTH_AGO_NO_LOCATION_DTO
);

export const REBROWSE_SESSIONS_DTOS = [
  REBROWSE_SESSION_DTO,
  REBROWSE_SESSION_HOUR_AGO_DTO,
  REBROWSE_SESSION_DAY_AGO_NO_LOCATION_DTO,
  REBROWSE_SESSION_MONTH_AGO_NO_LOCATION_DTO,
];

export const REBROWSE_SESSIONS = [
  REBROWSE_SESSION,
  REBROWSE_SESSION_HOUR_AGO,
  REBROWSE_SESSION_DAY_AGO_NO_LOCATION,
  REBROWSE_SESSION_MONTH_AGO_NO_LOCATION,
];

export * from './events';
export * from './user';
export * from './organization';

export const TFA_METHODS: TfaMethod[] = ['totp', 'sms'];
