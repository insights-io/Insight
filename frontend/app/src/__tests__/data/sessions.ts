import {
  DeviceClass,
  Session,
  SessionDTO,
  TimePrecision,
  UserAgentDTO,
} from '@rebrowse/types';
import { v4 as uuid } from 'uuid';
import { REBROWSE_ADMIN_DTO } from '__tests__/data/user';
import { subHours } from 'date-fns';
import { mapSession } from '@rebrowse/sdk';
import { countSessionsBy } from '__tests__/mocks/filter';

export const MAC__SAFARI: UserAgentDTO = {
  deviceName: 'Apple Macintosh',
  deviceBrand: 'Apple',
  deviceClass: DeviceClass.DESKTOP,
  operatingSystemName: 'Mac OS X',
  operatingSystemVersion: '10.14.6',
  agentName: 'Chrome',
  agentVersion: '87.0.664',
};

export const LINUX_FIREFOX: UserAgentDTO = {
  deviceName: 'Linux Desktop',
  deviceBrand: 'Unknown',
  deviceClass: DeviceClass.DESKTOP,
  operatingSystemName: 'Ubuntu',
  operatingSystemVersion: '??',
  agentName: 'Firefox',
  agentVersion: '15.0.1',
};

export const HTC_ONE_X10__CHROME: UserAgentDTO = {
  deviceName: 'HTC ONE X10',
  deviceBrand: 'HTC',
  deviceClass: DeviceClass.PHONE,
  operatingSystemName: 'Android',
  operatingSystemVersion: '6.0',
  agentName: 'Chrome Webview',
  agentVersion: '61.0.3163.98',
};

const HOURS_IN_DAY = 24;
const HOURS_IN_30_DAYS = HOURS_IN_DAY * 30;
const NOW = new Date();

export const REBROWSE_SESSIONS_DESKTOP_FROM_LJUBLJANA: SessionDTO[] = Array.from(
  { length: HOURS_IN_30_DAYS }
).map((_, index) => {
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
    userAgent: MAC__SAFARI,
  };
});

export const REBROWSE_SESSIONS_PHONE_FROM_ZAGREB: SessionDTO[] = Array.from({
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
    userAgent: MAC__SAFARI,
  };
});

export const REBROWSE_SESSIONS_DESKTOP_FROM_VIRGINIA: SessionDTO[] = Array.from(
  {
    length: HOURS_IN_30_DAYS,
  }
).map((_, index) => {
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
    userAgent: LINUX_FIREFOX,
  };
});

export const REBROWSE_SESSIONS_PHONE_NO_LOCATION: SessionDTO[] = Array.from({
  length: HOURS_IN_30_DAYS,
}).map((_, index) => {
  const offsetHours = Math.ceil(Math.random() * 10) + index;

  return {
    id: uuid(),
    createdAt: subHours(NOW, offsetHours).toISOString(),
    deviceId: '123',
    location: { ip: '13.77.88.76' },
    organizationId: REBROWSE_ADMIN_DTO.organizationId,
    userAgent: HTC_ONE_X10__CHROME,
  };
});

export const REBROWSE_SESSIONS_DTOS: SessionDTO[] = [
  ...REBROWSE_SESSIONS_DESKTOP_FROM_LJUBLJANA,
  ...REBROWSE_SESSIONS_PHONE_FROM_ZAGREB,
  ...REBROWSE_SESSIONS_DESKTOP_FROM_VIRGINIA,
  ...REBROWSE_SESSIONS_PHONE_NO_LOCATION,
].sort(
  (a, b) => new Date(b.createdAt).valueOf() - new Date(a.createdAt).valueOf()
);

export const REBROWSE_SESSIONS: Session[] = REBROWSE_SESSIONS_DTOS.map(
  mapSession
);

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
});

export const COUNT_PAGE_VISITS_BY_DATE = [...COUNT_SESSIONS_BY_DATE];
