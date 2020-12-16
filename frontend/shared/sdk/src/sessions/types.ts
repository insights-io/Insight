import type { SearchBean } from '@rebrowse/types';

import type { RequestOptions } from '../types';

export type SessionSearchBean = SearchBean<{
  createdAt?: unknown;
  'location.countryName'?: unknown;
  'location.continentName'?: unknown;
  'location.city'?: unknown;
  'location.regionName'?: unknown;
  'location.ip'?: unknown;
  'user_agent.browserName'?: unknown;
  'user_agent.operatingSystemName'?: unknown;
  'user_agent.deviceClass'?: unknown;
}>;

export type SessionsSearchRequestOptions = Omit<
  RequestOptions,
  'searchParams'
> & {
  search?: SessionSearchBean;
};

export type EventSeachBean = SearchBean<{ 'event.e'?: unknown }>;

export type SearchEventsRequestOptions = Omit<
  RequestOptions,
  'searchParams'
> & {
  search?: EventSeachBean;
};
