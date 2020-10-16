import { mapOrganization, mapTeamInvite } from '@insight/sdk';
import { subDays } from 'date-fns';
import type {
  Organization,
  OrganizationDTO,
  TeamInviteDTO,
} from '@insight/types';

export const INSIGHT_ORGANIZATION_DTO: OrganizationDTO = {
  id: '000000',
  name: 'Insight',
  defaultRole: 'member',
  openMembership: false,
  createdAt: new Date().toUTCString(),
  updatedAt: new Date().toUTCString(),
};

export const INSIGHT_ORGANIZATION: Organization = mapOrganization(
  INSIGHT_ORGANIZATION_DTO
);

export const NAMELESS_INSIGHT_ORGANIZATION: Organization = {
  ...INSIGHT_ORGANIZATION,
  name: undefined,
};

export const STANDARD_TEAM_INVITE_DTO: TeamInviteDTO = {
  email: 'standard@gmail.com',
  role: 'member',
  createdAt: new Date().toUTCString(),
  creator: '123',
  org: '000000',
  token: '1',
};

export const STANDARD_TEAM_INVITE = mapTeamInvite(STANDARD_TEAM_INVITE_DTO);

export const ADMIN_TEAM_INVITE_DTO: TeamInviteDTO = {
  email: 'admin@gmail.com',
  role: 'admin',
  createdAt: new Date().toUTCString(),
  creator: '123',
  org: '000000',
  token: '2',
};

export const ADMIN_TEAM_INVITE = mapTeamInvite(ADMIN_TEAM_INVITE_DTO);

export const EXPIRED_TEAM_INVITE_DTO: TeamInviteDTO = {
  email: 'expired@gmail.com',
  role: 'member',
  createdAt: subDays(new Date(), 1).toUTCString(),
  creator: '123',
  org: '000000',
  token: '3',
};
