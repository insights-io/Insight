import { SessionApi } from 'api';
import { QueryParam, TimePrecision } from '@rebrowse/types';
import type { RequestOptions } from '@rebrowse/sdk';

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
  }).then((httpResponse) => httpResponse.data.data);
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
  }).then((httpResponse) => httpResponse.data.data);
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
  }).then((httpResponse) => httpResponse.data.data);
};

export const countPageVisitsByDate = (
  createdAt: QueryParam,
  options?: RequestOptions
) => {
  return SessionApi.pageVisit
    .count({
      ...options,
      search: {
        groupBy: ['createdAt'],
        dateTrunc: TimePrecision.DAY,
        createdAt,
      },
    })
    .then((httpResponse) => httpResponse.data.data);
};

export const countPageVisitsByReferrer = (options?: RequestOptions) => {
  return SessionApi.pageVisit
    .count({
      ...options,
      search: { groupBy: ['referrer'] },
    })
    .then((httpResponse) => httpResponse.data.data);
};

export const countPageVisitsByPath = (options?: RequestOptions) => {
  return SessionApi.pageVisit
    .count({
      ...options,
      search: { groupBy: ['path'] },
    })
    .then((httpResponse) => httpResponse.data.data);
};
