import { UserRole } from './user';

export type OrganizationDTO = {
  id: string;
  name: string;
  createdAt: string;
};

export type Organization = Omit<OrganizationDTO, 'createdAt'> & {
  createdAt: Date;
};

export type TeamInviteCreateDTO = {
  email: string;
  role: UserRole;
};

export type TeamInviteDTO = TeamInviteCreateDTO & {
  token: string;
  org: string;
  creator: string;
  createdAt: string;
};

export type TeamInvite = Omit<TeamInviteDTO, 'createdAt'> & {
  createdAt: Date;
};
