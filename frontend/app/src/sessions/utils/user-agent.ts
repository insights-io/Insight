import { DeviceClass, UserAgentDTO } from '@rebrowse/types';
import {
  FaAndroid,
  FaApple,
  FaBookReader,
  FaChrome,
  FaDesktop,
  FaFirefox,
  FaGamepad,
  FaMobileAlt,
  FaQuestion,
  FaStopwatch,
  FaTablet,
  FaTv,
  FaWindows,
} from 'react-icons/fa';
import type { IconType } from 'react-icons/lib';

const DEVICE_CLASS_TO_ICON_MAP: Record<
  UserAgentDTO['deviceClass'],
  IconType
> = {
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
  return DEVICE_CLASS_TO_ICON_MAP[key] || FaQuestion;
};

const OPERATING_SYSTEM_NAME_TO_ICON_MAP: Record<
  UserAgentDTO['operatingSystemName'],
  IconType
> = {
  'Mac OS X': FaApple,
  Android: FaAndroid,
  'Windows NT': FaWindows,
};

export const getOperatingSystemIcon = (
  key: UserAgentDTO['operatingSystemName']
) => {
  return OPERATING_SYSTEM_NAME_TO_ICON_MAP[key] || FaQuestion;
};

const AGENT_NAME_TO_ICON_MAP: Record<UserAgentDTO['agentName'], IconType> = {
  Chrome: FaChrome,
  'Chrome Webview': FaChrome,
  Firefox: FaFirefox,
};

export const getAgentNameIcon = (key: UserAgentDTO['agentName']) => {
  return AGENT_NAME_TO_ICON_MAP[key] || FaQuestion;
};
