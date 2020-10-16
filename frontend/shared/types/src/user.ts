import type { OrganizationDTO } from './organization';

export type LoginResponseDTO = boolean | { challengeId: string };

export type PhoneNumber = {
  countryCode: string;
  digits: string;
};

export type UserRole = 'admin' | 'member';

export type UserDTO = {
  id: string;
  email: string;
  role: UserRole;
  fullName: string | undefined;
  createdAt: string;
  updatedAt: string;
  organizationId: string;
  phoneNumber: PhoneNumber | null;
  phoneNumberVerified: boolean;
};

export type User = Omit<UserDTO, 'createdAt' | 'updatedAt'> & {
  createdAt: Date;
  updatedAt: Date;
};

export type CodeValidityDTO = {
  validitySeconds: number;
};

export type SessionInfoDTO = {
  user: UserDTO;
  organization: OrganizationDTO;
};
