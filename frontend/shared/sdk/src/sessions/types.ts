import type { QueryParam, SearchBean } from '@rebrowse/types';
import type { RequestOptions } from 'types';

export type SessionSearchQueryParams = {
  createdAt?: QueryParam;

  'location.country_name'?: QueryParam;
  'location.countryName'?: QueryParam;

  'location.continent_name'?: QueryParam;
  'location.continentName'?: QueryParam;

  'location.city'?: QueryParam;

  'location.region_name'?: QueryParam;
  'location.regionName'?: QueryParam;

  'location.ip'?: QueryParam;

  'user_agent.agent_version'?: QueryParam;

  'user_agent.agent_name'?: QueryParam;
  'userAgent.agent_name'?: QueryParam;

  'user_agent.operating_system_version'?: QueryParam;

  'user_agent.operating_system_name'?: QueryParam;
  'user_agent.operatingSystemName'?: QueryParam;
  'userAgent.operatingSystemName'?: QueryParam;
  'userAgent.operating_system_name'?: QueryParam;

  'user_agent.device_name'?: QueryParam;

  'user_agent.device_brand'?: QueryParam;

  'user_agent.deviceClass'?: QueryParam;
  'user_agent.device_class'?: QueryParam;
  'userAgent.deviceClass'?: QueryParam;
  'userAgent.device_class'?: QueryParam;
};

export type SessionSearchBean<
  GroupBy extends (keyof SessionSearchQueryParams)[] = []
> = SearchBean<SessionSearchQueryParams, GroupBy>;

export type SessionsSearchRequestOptions<
  GroupBy extends (keyof SessionSearchQueryParams)[] = []
> = Omit<RequestOptions, 'searchParams'> & {
  search?: SessionSearchBean<GroupBy>;
};
