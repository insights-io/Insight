import type { SearchBean } from '@rebrowse/types';
import type { RequestOptions } from 'types';

export type SessionSearchQueryParams = {
  createdAt?: unknown;

  'location.country_name'?: unknown;
  'location.countryName'?: unknown;

  'location.continent_name'?: unknown;
  'location.continentName'?: unknown;

  'location.city'?: unknown;

  'location.region_name'?: unknown;
  'location.regionName'?: unknown;

  'location.ip'?: unknown;

  'user_agent.agent_version'?: unknown;

  'user_agent.agent_name'?: unknown;
  'userAgent.agent_name'?: unknown;

  'user_agent.operating_system_version'?: unknown;

  'user_agent.operating_system_name'?: unknown;
  'user_agent.operatingSystemName'?: unknown;
  'userAgent.operatingSystemName'?: unknown;
  'userAgent.operating_system_name'?: unknown;

  'user_agent.device_name'?: unknown;

  'user_agent.device_brand'?: unknown;

  'user_agent.deviceClass'?: unknown;
  'user_agent.device_class'?: unknown;
  'userAgent.deviceClass'?: unknown;
  'userAgent.device_class'?: unknown;
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
