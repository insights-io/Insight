import type { QueryParam, SearchBean } from '@rebrowse/types';

import type { RequestOptions } from '../types';

export type SessionSearchBean = SearchBean & {
  // eslint-disable-next-line camelcase
  created_at?: QueryParam;
  'location.countryName'?: QueryParam;
  'location.continentName'?: QueryParam;
  'location.city'?: QueryParam;
  'location.regionName'?: QueryParam;
  'location.ip'?: QueryParam;
  'user_agent.browserName'?: QueryParam;
  'user_agent.operatingSystemName'?: QueryParam;
  'user_agent.deviceClass'?: QueryParam;
};

export type SessionsSearchRequestOptions = Omit<
  RequestOptions,
  'searchParams'
> & {
  search?: SessionSearchBean;
};

export type EventSeachBean = SearchBean & { 'event.e'?: QueryParam };

export type SearchEventsRequestOptions = Omit<
  RequestOptions,
  'searchParams'
> & {
  search?: EventSeachBean;
};
