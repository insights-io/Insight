import type { GroupByData } from 'modules/insights/components/charts/GroupByPieChart';
import type {
  CountByDateDataPoint,
  CountByDeviceClassDataPoint,
  CountByLocationDataPoint,
} from 'modules/insights/pages/InsightsPage';
import type { Session, SessionDTO, UserAgentDTO } from '@rebrowse/types';
import { v4 as uuid } from 'uuid';
import { REBROWSE_ADMIN_DTO } from 'test/data/user';
import { differenceInDays, subHours } from 'date-fns';
import { mapSession } from '@rebrowse/sdk';

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

const HOURS_IN_DAY = 24;
const HOURS_IN_30_DAYS = HOURS_IN_DAY * 30;
const NOW = new Date();

const REBROWSE_SESSIONS_DESKTOP_FROM_LJUBLJANA: SessionDTO[] = Array.from({
  length: HOURS_IN_30_DAYS,
}).map((_, index) => {
  const offsetHours = Math.ceil(Math.random() * 10) * index;

  return {
    id: uuid(),
    createdAt: subHours(NOW, -offsetHours).toISOString(),
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
    userAgent: DESKTOP_USER_AGENT,
  };
});

const REBROWSE_SESSIONS_PHONE_FROM_ZAGREB: SessionDTO[] = Array.from({
  length: HOURS_IN_DAY,
}).map((_, index) => {
  const offsetHours = Math.ceil(Math.random() * 10) * index;

  return {
    id: uuid(),
    createdAt: subHours(NOW, -offsetHours).toISOString(),
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
    userAgent: DESKTOP_USER_AGENT,
  };
});

const REBROWSE_SESSIONS_DESKTOP_FROM_VIRGINIA: SessionDTO[] = Array.from({
  length: HOURS_IN_30_DAYS,
}).map((_, index) => {
  const offsetHours = Math.ceil(Math.random() * 10) * index;

  return {
    id: uuid(),
    createdAt: subHours(NOW, -offsetHours).toISOString(),
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
    userAgent: DESKTOP_USER_AGENT,
  };
});

const REBROWSE_SESSIONS_PHONE_NO_LOCATION: SessionDTO[] = Array.from({
  length: HOURS_IN_30_DAYS,
}).map((_, index) => {
  const offsetHours = Math.ceil(Math.random() * 10) * index;

  return {
    id: uuid(),
    createdAt: subHours(NOW, -offsetHours).toISOString(),
    deviceId: '123',
    location: { ip: '13.77.88.76' },
    organizationId: REBROWSE_ADMIN_DTO.organizationId,
    userAgent: MOBILE_USER_AGENT,
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

export const COUNT_SESSIONS_BY_LOCATION = (() => {
  const map = REBROWSE_SESSIONS_DTOS.reduce((acc, item) => {
    const {
      location: { countryName = 'Unknown', continentName = 'Unknown' },
    } = item;
    const key = `${countryName}--${continentName}`;
    const value = acc[key];

    return {
      ...acc,
      [key]: (value || 0) + 1,
    };
  }, {} as Record<string, number>);

  return Object.keys(map).reduce((acc, key) => {
    const [countryName, continentName] = key.split('--');
    return [
      ...acc,
      {
        'location.continentName': continentName,
        'location.countryName': countryName,
        count: map[key],
      },
    ];
  }, [] as CountByLocationDataPoint[]);
})();

console.log({ COUNT_SESSIONS_BY_LOCATION });

export const COUNT_SESSIONS_BY_DEVICE_CLASS = REBROWSE_SESSIONS_DTOS.reduce(
  (acc, item) => {
    // eslint-disable-next-line no-restricted-syntax
    for (const existingItem of acc) {
      if (
        existingItem['userAgent.deviceClass'] === item.userAgent.deviceClass
      ) {
        existingItem.count++;
        return acc;
      }
    }

    return [
      ...acc,
      { count: 1, 'userAgent.deviceClass': item.userAgent.deviceClass },
    ];
  },
  [] as CountByDeviceClassDataPoint[]
);

export const COUNT_SESSIONS_BY_DATE = REBROWSE_SESSIONS_DTOS.reduce(
  (acc, item) => {
    // eslint-disable-next-line no-restricted-syntax
    for (const existingItem of acc) {
      if (
        differenceInDays(
          new Date(existingItem.createdAt),
          new Date(item.createdAt)
        ) === 0
      ) {
        existingItem.count++;
        return acc;
      }
    }

    return [...acc, { count: 1, createdAt: item.createdAt }];
  },
  [] as CountByDateDataPoint[]
).sort(
  (a, b) => new Date(a.createdAt).valueOf() - new Date(b.createdAt).valueOf()
);

export const COUNT_PAGE_VISITS_BY_DATE = [...COUNT_SESSIONS_BY_DATE];
