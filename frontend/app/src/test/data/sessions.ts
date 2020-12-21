import type { GroupByData } from 'modules/insights/components/charts/GroupByPieChart';
import {
  Session,
  SessionDTO,
  TimePrecision,
  UserAgentDTO,
} from '@rebrowse/types';
import { v4 as uuid } from 'uuid';
import { REBROWSE_ADMIN_DTO } from 'test/data/user';
import { subHours } from 'date-fns';
import { mapSession } from '@rebrowse/sdk';
import { countSessionsBy } from 'test/mocks/filter';

export const DESKTOP_MAC_OSX_CHROME: UserAgentDTO = {
  deviceClass: 'Desktop',
  operatingSystemName: 'Mac OS X',
  browserName: 'Chrome',
};

export const DESKTOP_MAC_OSX_FIREFOX: UserAgentDTO = {
  deviceClass: 'Desktop',
  operatingSystemName: 'Mac OS X',
  browserName: 'Firefox',
};

export const MOBILE_ANDROID_CHROME: UserAgentDTO = {
  deviceClass: 'Phone',
  operatingSystemName: 'Android',
  browserName: 'Chrome',
};

const HOURS_IN_DAY = 24;
const HOURS_IN_30_DAYS = HOURS_IN_DAY * 30;
const NOW = new Date();

const REBROWSE_SESSIONS_DESKTOP_FROM_LJUBLJANA: SessionDTO[] = Array.from({
  length: HOURS_IN_30_DAYS,
}).map((_, index) => {
  const offsetHours = Math.ceil(Math.random() * 10) + index;

  return {
    id: uuid(),
    createdAt: subHours(NOW, offsetHours).toISOString(),
    deviceId: '123',
    location: {
      ip: '82.192.62.51',
      city: 'Ljubljana',
      zip: '1000',
      latitude: 46.051429748535156,
      longitude: 14.505970001220703,
      countryName: 'Slovenia',
      regionName: 'Ljubljana',
      continentName: 'Europe',
    },
    organizationId: REBROWSE_ADMIN_DTO.organizationId,
    userAgent: DESKTOP_MAC_OSX_CHROME,
  };
});

const REBROWSE_SESSIONS_PHONE_FROM_ZAGREB: SessionDTO[] = Array.from({
  length: HOURS_IN_DAY,
}).map((_, index) => {
  const offsetHours = Math.ceil(Math.random() * 10) + index;

  return {
    id: uuid(),
    createdAt: subHours(NOW, offsetHours).toISOString(),
    deviceId: '123',
    location: {
      ip: '82.192.62.51',
      city: 'Zagreb',
      zip: '1000',
      latitude: 46.051429748535156,
      longitude: 14.505970001220703,
      countryName: 'Croatia',
      regionName: 'Zagreb',
      continentName: 'Europe',
    },
    organizationId: REBROWSE_ADMIN_DTO.organizationId,
    userAgent: DESKTOP_MAC_OSX_CHROME,
  };
});

const REBROWSE_SESSIONS_DESKTOP_FROM_VIRGINIA: SessionDTO[] = Array.from({
  length: HOURS_IN_30_DAYS,
}).map((_, index) => {
  const offsetHours = Math.ceil(Math.random() * 10) + index;

  return {
    id: uuid(),
    createdAt: subHours(NOW, offsetHours).toISOString(),
    deviceId: '123',
    location: {
      ip: '13.77.88.76',
      city: 'Boydton',
      zip: '23917',
      latitude: 36.667999267578125,
      longitude: -78.38899993896484,
      countryName: 'United States',
      regionName: 'Virginia',
      continentName: 'North America',
    },
    organizationId: REBROWSE_ADMIN_DTO.organizationId,
    userAgent: DESKTOP_MAC_OSX_FIREFOX,
  };
});

const REBROWSE_SESSIONS_PHONE_NO_LOCATION: SessionDTO[] = Array.from({
  length: HOURS_IN_30_DAYS,
}).map((_, index) => {
  const offsetHours = Math.ceil(Math.random() * 10) + index;

  return {
    id: uuid(),
    createdAt: subHours(NOW, offsetHours).toISOString(),
    deviceId: '123',
    location: { ip: '13.77.88.76' },
    organizationId: REBROWSE_ADMIN_DTO.organizationId,
    userAgent: MOBILE_ANDROID_CHROME,
  };
});

export const REBROWSE_SESSIONS_DTOS: SessionDTO[] = [
  ...REBROWSE_SESSIONS_DESKTOP_FROM_LJUBLJANA,
  ...REBROWSE_SESSIONS_PHONE_FROM_ZAGREB,
  ...REBROWSE_SESSIONS_DESKTOP_FROM_VIRGINIA,
  ...REBROWSE_SESSIONS_PHONE_NO_LOCATION,
];

export const REBROWSE_SESSIONS: Session[] = [
  ...REBROWSE_SESSIONS_DESKTOP_FROM_LJUBLJANA.map(mapSession),
  ...REBROWSE_SESSIONS_PHONE_FROM_ZAGREB.map(mapSession),
  ...REBROWSE_SESSIONS_DESKTOP_FROM_VIRGINIA.map(mapSession),
  ...REBROWSE_SESSIONS_PHONE_NO_LOCATION.map(mapSession),
];

export const GROUP_BY_COUNTRY: GroupByData = {
  Slovenia: 1,
  Crotia: 5,
  Hungary: 3,
  Germany: 4,
};

export const COUNT_SESSIONS_BY_LOCATION = countSessionsBy(
  REBROWSE_SESSIONS_DTOS,
  { groupBy: ['location.continentName', 'location.countryName'] }
);

export const COUNT_SESSIONS_BY_DEVICE_CLASS = countSessionsBy(
  REBROWSE_SESSIONS_DTOS,
  { groupBy: ['userAgent.deviceClass'] }
);

export const COUNT_SESSIONS_BY_DATE = countSessionsBy(REBROWSE_SESSIONS_DTOS, {
  groupBy: ['createdAt'],
  dateTrunc: TimePrecision.DAY,
}).sort(
  (a, b) => new Date(a.createdAt).valueOf() - new Date(b.createdAt).valueOf()
);

export const COUNT_PAGE_VISITS_BY_DATE = [...COUNT_SESSIONS_BY_DATE];
