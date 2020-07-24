export type UserAgentDTO = {
  deviceClass: 'Desktop' | string;
  operatingSystemName: string;
  browserName: string;
};

export type LocationDTO = {
  ip: string;
  countryName?: string;
  regionName?: string;
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
