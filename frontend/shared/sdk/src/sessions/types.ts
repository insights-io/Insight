import type { SearchBean } from '@rebrowse/types';
import type { RequestOptions } from 'types';

export type SessionSearchQueryParams = {
  createdAt?: unknown;
  'location.countryName'?: unknown;
  'location.continentName'?: unknown;
  'location.city'?: unknown;
  'location.regionName'?: unknown;
  'location.ip'?: unknown;
  'user_agent.browserName'?: unknown;
  'userAgent.browserName'?: unknown;
  'user_agent.operatingSystemName'?: unknown;
  'userAgent.operatingSystemName'?: unknown;
  'user_agent.deviceClass'?: unknown;
  'userAgent.deviceClass'?: unknown;
};

export type SessionSearchBean<
  GroupBy extends (keyof SessionSearchQueryParams)[] = []
> = SearchBean<SessionSearchQueryParams, GroupBy>;

export type SessionsSearchRequestOptions<
  GroupBy extends (keyof SessionSearchQueryParams)[] = []
> = Omit<RequestOptions, 'searchParams'> & {
  search?: SessionSearchBean<GroupBy>;
};

export type EventSearchQueryParams = {
  'event.e'?: unknown;
};

export type EventSeachBean<
  GroupBy extends (keyof EventSearchQueryParams)[] = []
> = SearchBean<EventSearchQueryParams, GroupBy>;

export type SearchEventsRequestOptions<
  GroupBy extends (keyof EventSearchQueryParams)[] = []
> = Omit<RequestOptions, 'searchParams'> & {
  search?: EventSeachBean<GroupBy>;
};
