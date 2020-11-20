import { mapOrganization, mapTeamInvite } from '@rebrowse/sdk';
import { addDays, subDays } from 'date-fns';
import type {
  Organization,
  OrganizationDTO,
  TeamInviteDTO,
} from '@rebrowse/types';

export const REBROWSE_ORGANIZATION_DTO: OrganizationDTO = {
  id: '000000',
  name: 'Rebrowse',
  defaultRole: 'member',
  openMembership: false,
  enforceMultiFactorAuthentication: false,
  createdAt: new Date().toUTCString(),
  updatedAt: new Date().toUTCString(),
};

export const REBROWSE_ORGANIZATION: Organization = mapOrganization(
  REBROWSE_ORGANIZATION_DTO
);

export const NAMELESS_ORGANIZATION: Organization = {
  ...REBROWSE_ORGANIZATION,
  name: undefined,
};

export const STANDARD_TEAM_INVITE_DTO: TeamInviteDTO = {
  email: 'standard@gmail.com',
  role: 'member',
  createdAt: new Date().toUTCString(),
  expiresAt: addDays(new Date(), 1).toUTCString(),
  creator: '123',
  organizationId: '000000',
  token: '1',
  valid: true,
};

export const STANDARD_TEAM_INVITE = mapTeamInvite(STANDARD_TEAM_INVITE_DTO);

export const ADMIN_TEAM_INVITE_DTO: TeamInviteDTO = {
  email: 'admin@gmail.com',
  role: 'admin',
  createdAt: new Date().toUTCString(),
  expiresAt: addDays(new Date(), 1).toUTCString(),
  creator: '123',
  organizationId: '000000',
  token: '2',
  valid: true,
};

export const ADMIN_TEAM_INVITE = mapTeamInvite(ADMIN_TEAM_INVITE_DTO);

export const EXPIRED_TEAM_INVITE_DTO: TeamInviteDTO = {
  email: 'expired@gmail.com',
  role: 'member',
  createdAt: subDays(new Date(), 2).toUTCString(),
  expiresAt: subDays(new Date(), 1).toUTCString(),
  creator: '123',
  organizationId: '000000',
  token: '3',
  valid: false,
};
