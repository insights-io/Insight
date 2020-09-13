export type UserAgentDTO = {
  deviceClass: 'Desktop' | 'Phone' | string;
  operatingSystemName: string;
  browserName: string;
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

export type SsoMethod = 'saml' | 'google' | 'microsoft' | 'github';

export type SsoSetupDTO = {
  organizationId: string;
  domain: string;
  createdAt: string;
  method: SsoMethod;
  configurationEndpoint?: string;
};

export type SsoSetup = Omit<SsoSetupDTO, 'createdAt'> & {
  createdAt: Date;
};
