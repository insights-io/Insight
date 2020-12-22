export enum DeviceClass {
  DESKTOP = 'Desktop',
  ANONYMIZED = 'Anonymized',
  MOBILE = 'Mobile',
  TABLET = 'Tablet',
  PHONE = 'Phone',
  WATCH = 'Watch',
  VIRTUAL_REALITY = 'Virtual Reality',
  E_READER = 'eReader',
  SET_TOP_BOX = 'Set-top box',
  TV = 'TV',
  GAME_CONSOLE = 'Game Console',
  HANDHELD_GAME_CONSOLE = 'Handheld Game Console',
  UNKNOWN = 'Unknown',
  UNCLASSIFIED = 'Unclassified',
}

export type UserAgentDTO = {
  deviceClass: DeviceClass;
  deviceName: string;
  deviceBrand: 'Apple' | 'Samsung' | 'Google';
  operatingSystemName:
    | 'Mac OS X'
    | 'Nintendo NX'
    | 'Android'
    | 'Windows NT'
    | 'Ubuntu'
    | string;
  operatingSystemVersion: string;
  agentName: 'Chrome' | 'Firefox' | string;
  agentVersion: string;
};

export type LocationDTO = {
  ip: string;
  countryName?: string;
  regionName?: string;
  continentName?: string;
  city?: string;
  zip?: string;
  latitude?: number;
  longitude?: number;
};

export type SessionDTO = {
  id: string;
  deviceId: string;
  organizationId: string;
  location: LocationDTO;
  userAgent: UserAgentDTO;
  createdAt: string;
};

export type Session = Omit<SessionDTO, 'createdAt'> & {
  createdAt: Date;
};

export type OAuthSsoMethod = 'google' | 'microsoft' | 'github';
export type SamlSsoMethod = 'saml';

export type SsoMethod = SamlSsoMethod | OAuthSsoMethod;

export type SamlMethod = 'okta' | 'onelogin' | 'auth0' | 'custom';

type SsoSetupBase = {
  organizationId: string;
  domain: string;
  createdAt: string;
};

export type OAuthSsoSetupDTO = SsoSetupBase & {
  method: OAuthSsoMethod;
};

export type SamlConfigurationDTO = {
  method: SamlMethod;
  metadataEndpoint: string;
};

export type SamlSsoSetupDTO = SsoSetupBase & {
  method: SamlSsoMethod;
  saml: SamlConfigurationDTO;
};

export type SsoSetupDTO = OAuthSsoSetupDTO | SamlSsoSetupDTO;

export type SsoSetup = Omit<SsoSetupDTO, 'createdAt'> & {
  createdAt: Date;
};
