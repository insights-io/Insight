import { DeviceClass, UserAgentDTO } from '@rebrowse/types';
import {
  FaAndroid,
  FaApple,
  FaBookReader,
  FaChrome,
  FaDesktop,
  FaGamepad,
  FaMobileAlt,
  FaQuestion,
  FaStopwatch,
  FaTablet,
  FaTv,
} from 'react-icons/fa';
import type { IconType } from 'react-icons/lib';

const DEVICE_CLASS_ICON_MAP: Record<UserAgentDTO['deviceClass'], IconType> = {
  [DeviceClass.DESKTOP]: FaDesktop,
  [DeviceClass.PHONE]: FaMobileAlt,
  [DeviceClass.MOBILE]: FaMobileAlt,
  [DeviceClass.GAME_CONSOLE]: FaGamepad,
  [DeviceClass.ANONYMIZED]: FaQuestion,
  [DeviceClass.E_READER]: FaBookReader,
  [DeviceClass.WATCH]: FaStopwatch,
  [DeviceClass.VIRTUAL_REALITY]: FaGamepad,
  [DeviceClass.SET_TOP_BOX]: FaTv,
  [DeviceClass.TV]: FaTv,
  [DeviceClass.TABLET]: FaTablet,
  [DeviceClass.HANDHELD_GAME_CONSOLE]: FaGamepad,
  [DeviceClass.UNKNOWN]: FaQuestion,
  [DeviceClass.UNCLASSIFIED]: FaQuestion,
};

export const getDeviceClassIcon = (key: UserAgentDTO['deviceClass']) => {
  return DEVICE_CLASS_ICON_MAP[key] || FaQuestion;
};

const OPERATING_SYTEM_ICON_MAP: Record<
  UserAgentDTO['operatingSystemName'],
  IconType
> = {
  'Mac OS X': FaApple,
  Android: FaAndroid,
};

export const getOperatingSystemIcon = (
  key: UserAgentDTO['operatingSystemName']
) => {
  return OPERATING_SYTEM_ICON_MAP[key] || FaQuestion;
};

const AGENT_NAME_ICON_MAP: Record<UserAgentDTO['agentName'], IconType> = {
  Chrome: FaChrome,
};

export const getAgentNameIcon = (key: UserAgentDTO['agentName']) => {
  return AGENT_NAME_ICON_MAP[key] || FaQuestion;
};
