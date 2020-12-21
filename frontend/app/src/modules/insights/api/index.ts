import { PagesApi, SessionApi } from 'api';
import type { SessionsSearchRequestOptions } from '@rebrowse/sdk';
import type {
  CountByDateDataPointDTO,
  CountByDeviceClassDataPoint,
  CountByLocationDataPoint,
} from 'modules/insights/pages/InsightsPage';
import { QueryParam, TimePrecision } from '@rebrowse/types';

export const countSessionsByLocation = (
  createdAt: QueryParam,
  options?: Omit<SessionsSearchRequestOptions, 'search'>
) => {
  return SessionApi.count<CountByLocationDataPoint[]>({
    ...options,
    search: {
      groupBy: ['location.countryName', 'location.continentName'],
      createdAt,
    },
  });
};

export const countSessionsByDeviceClass = (
  createdAt: QueryParam,
  options?: Omit<SessionsSearchRequestOptions, 'search'>
) => {
  return SessionApi.count<CountByDeviceClassDataPoint[]>({
    ...options,
    search: {
      groupBy: ['userAgent.deviceClass'],
      createdAt,
    },
  });
};

export const countSessionsByDate = (
  createdAt: QueryParam,
  options?: Omit<SessionsSearchRequestOptions, 'search'>
) => {
  return SessionApi.count<CountByDateDataPointDTO[]>({
    ...options,
    search: {
      groupBy: ['createdAt'],
      dateTrunc: TimePrecision.DAY,
      createdAt,
    },
  });
};

export const countPageVisitsByDate = (
  createdAt: QueryParam,
  options?: Omit<SessionsSearchRequestOptions, 'search'>
) => {
  return PagesApi.count<CountByDateDataPointDTO[]>({
    ...options,
    search: {
      groupBy: ['createdAt'],
      dateTrunc: TimePrecision.DAY,
      createdAt,
    },
  });
};
