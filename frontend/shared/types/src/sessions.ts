export type SessionDTO = {
  id: string;
  deviceId: string;
  organizationId: string;
  ipAddress: string;
  userAgent: string;
  createdAt: string;
};

export type Session = Omit<SessionDTO, 'createdAt'> & {
  createdAt: Date;
};
