import { PagesApi, SessionApi } from 'api';
import type { RequestOptions } from '@rebrowse/sdk';
import { QueryParam, TimePrecision } from '@rebrowse/types';

export const countSessionsByLocation = (
  createdAt: QueryParam,
  options?: RequestOptions
) => {
  return SessionApi.count({
    ...options,
    search: {
      groupBy: ['location.countryName', 'location.continentName'],
      createdAt,
    },
  });
};

export const countSessionsByDeviceClass = (
  createdAt: QueryParam,
  options?: RequestOptions
) => {
  return SessionApi.count({
    ...options,
    search: {
      groupBy: ['userAgent.deviceClass'],
      createdAt,
    },
  });
};

export const countSessionsByDate = (
  createdAt: QueryParam,
  options?: RequestOptions
) => {
  return SessionApi.count({
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
  options?: RequestOptions
) => {
  return PagesApi.count({
    ...options,
    search: {
      groupBy: ['createdAt'],
      dateTrunc: TimePrecision.DAY,
      createdAt,
    },
  });
};
