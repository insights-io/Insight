export type UserAgentDTO = {
  deviceClass: 'Desktop' | string;
  operatingSystemName: string;
  browserName: string;
};

export type SessionDTO = {
  id: string;
  deviceId: string;
  organizationId: string;
  ipAddress: string;
  userAgent: UserAgentDTO;
  createdAt: string;
};

export type Session = Omit<SessionDTO, 'createdAt'> & {
  createdAt: Date;
};
