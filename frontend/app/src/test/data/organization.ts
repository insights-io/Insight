import { mapOrganization, mapTeamInvite } from '@insight/sdk';
import type { OrganizationDTO, TeamInviteDTO } from '@insight/types';
import { subDays } from 'date-fns';

export const INSIGHT_ORGANIZATION_DTO: OrganizationDTO = {
  id: '000000',
  name: 'Insight',
  createdAt: new Date().toUTCString(),
  updatedAt: new Date().toUTCString(),
};

export const INSIGHT_ORGANIZATION = mapOrganization(INSIGHT_ORGANIZATION_DTO);

export const STANDARD_TEAM_INVITE_DTO: TeamInviteDTO = {
  email: 'standard@gmail.com',
  role: 'standard',
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
  role: 'standard',
  createdAt: subDays(new Date(), 1).toUTCString(),
  creator: '123',
  org: '000000',
  token: '3',
};
