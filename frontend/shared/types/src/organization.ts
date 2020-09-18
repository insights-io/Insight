import { UserRole } from './user';

export type SubscriptionPlan = 'free' | 'business' | 'enterprise';

export type OrganizationDTO = {
  id: string;
  name: string;
  plan: SubscriptionPlan;
  createdAt: string;
  updatedAt: string;
};

export type Organization = Omit<OrganizationDTO, 'createdAt' | 'updatedAt'> & {
  createdAt: Date;
  updatedAt: Date;
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
