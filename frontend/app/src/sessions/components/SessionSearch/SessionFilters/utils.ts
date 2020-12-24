import type { ParsedUrlQuery } from 'querystring';

import {
  FaMapMarker,
  FaMap,
  FaFlag,
  FaGlobe,
  FaLocationArrow,
  FaInternetExplorer,
  FaDesktop,
  FaWindows,
  FaApple,
  FaPhone,
} from 'react-icons/fa';
import { IconType } from 'react-icons/lib';
import { v4 as uuid } from 'uuid';

export const FILTER_OPTIONS = [
  { label: 'City', icon: FaMapMarker, key: 'location.city' },
  { label: 'State/Region', icon: FaMap, key: 'location.region_name' },
  { label: 'Country', icon: FaFlag, key: 'location.country_name' },
  { label: 'Continent', icon: FaGlobe, key: 'location.continent_name' },
  { label: 'IP address', icon: FaLocationArrow, key: 'location.ip' },
  {
    label: 'Browser name',
    icon: FaInternetExplorer,
    key: 'user_agent.agent_name',
  },
  {
    label: 'Browser version',
    icon: FaInternetExplorer,
    key: 'user_agent.agent_version',
  },
  {
    label: 'Device class',
    icon: FaDesktop,
    key: 'user_agent.device_class',
  },
  {
    label: 'Device brand',
    icon: FaApple,
    key: 'user_agent.device_brand',
  },
  {
    label: 'Device name',
    icon: FaPhone,
    key: 'user_agent.device_name',
  },
  {
    label: 'Operating System name',
    icon: FaWindows,
    key: 'user_agent.operating_system_name',
  },
  {
    label: 'Operating System version',
    icon: FaWindows,
    key: 'user_agent.operating_system_version',
  },
] as const;

export type FilterOption = typeof FILTER_OPTIONS[number];
export type FilterKey = FilterOption['key'];

export const FILTER_LOOKUPS = FILTER_OPTIONS.reduce((acc, option) => {
  return {
    ...acc,
    [option.key]: { label: option.label, icon: option.icon },
  };
}, {} as Record<FilterKey, { label: string; icon: IconType }>);

export type SessionFilter = {
  id: string;
  value: string;
  key: FilterKey | undefined;
};

export const generateNewFilter = (): SessionFilter => {
  return { id: uuid(), value: '', key: undefined };
};

export const parseQueryFilters = (query: ParsedUrlQuery) => {
  return Object.keys(query).reduce((acc, key) => {
    const maybeFilterKey = key as FilterKey;
    if (!FILTER_LOOKUPS[maybeFilterKey]) {
      return acc;
    }

    const maybeValue = query[maybeFilterKey];
    if (!maybeValue) {
      return acc;
    }

    const values = Array.isArray(maybeValue) ? maybeValue : [maybeValue];
    const newFilters = values.map((value) => ({
      key: maybeFilterKey,
      value,
      id: uuid(),
    }));

    return [...acc, ...newFilters];
  }, [] as SessionFilter[]);
};
