import {
  FaMapMarker,
  FaMap,
  FaFlag,
  FaGlobe,
  FaLocationArrow,
  FaInternetExplorer,
  FaDesktop,
  FaWindows,
} from 'react-icons/fa';
import { IconType } from 'react-icons/lib';
import { v4 as uuid } from 'uuid';

export const FILTER_OPTIONS = [
  { label: 'City', icon: FaMapMarker, key: 'location.city' },
  { label: 'State/Region', icon: FaMap, key: 'location.regionName' },
  { label: 'Country', icon: FaFlag, key: 'location.countryName' },
  { label: 'Continent', icon: FaGlobe, key: 'location.continentName' },
  { label: 'IP address', icon: FaLocationArrow, key: 'location.ip' },
  {
    label: 'Browser',
    icon: FaInternetExplorer,
    key: 'user_agent.browserName',
  },
  {
    label: 'Device',
    icon: FaDesktop,
    key: 'user_agent.deviceClass',
  },
  {
    label: 'Operating System',
    icon: FaWindows,
    key: 'user_agent.operatingSystemName',
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
