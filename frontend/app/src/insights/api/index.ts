import { client } from 'sdk';
import { QueryParam, TimePrecision } from '@rebrowse/types';
import type { RequestOptions } from '@rebrowse/sdk';

export const countSessionsByLocation = (
  createdAt: QueryParam,
  options?: RequestOptions
) => {
  return client.recording.sessions
    .count({
      ...options,
      search: {
        groupBy: ['location.countryName', 'location.continentName'],
        createdAt,
      },
    })
    .then((httpResponse) => httpResponse.data);
};

export const countSessionsByDeviceClass = (
  createdAt: QueryParam,
  options?: RequestOptions
) => {
  return client.recording.sessions
    .count({
      ...options,
      search: {
        groupBy: ['userAgent.deviceClass'],
        createdAt,
      },
    })
    .then((httpResponse) => httpResponse.data);
};

export const countSessionsByDate = (
  createdAt: QueryParam,
  options?: RequestOptions
) => {
  return client.recording.sessions
    .count({
      ...options,
      search: {
        groupBy: ['createdAt'],
        dateTrunc: TimePrecision.DAY,
        createdAt,
      },
    })
    .then((httpResponse) => httpResponse.data);
};

export const countPageVisitsByDate = (
  createdAt: QueryParam,
  options?: RequestOptions
) => {
  return client.recording.pageVisits
    .count({
      ...options,
      search: {
        groupBy: ['createdAt'],
        dateTrunc: TimePrecision.DAY,
        createdAt,
      },
    })
    .then((httpResponse) => httpResponse.data);
};

export const countPageVisitsByReferrer = (options?: RequestOptions) => {
  return client.recording.pageVisits
    .count({
      ...options,
      search: { groupBy: ['referrer'] },
    })
    .then((httpResponse) => httpResponse.data);
};

export const countPageVisitsByPath = (options?: RequestOptions) => {
  return client.recording.pageVisits
    .count({
      ...options,
      search: { groupBy: ['path'] },
    })
    .then((httpResponse) => httpResponse.data);
};
