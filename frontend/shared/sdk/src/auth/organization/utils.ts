import type {
  Organization,
  OrganizationDTO,
  TeamInvite,
  TeamInviteDTO,
} from '@insight/types';

export const mapOrganization = (
  organization: Organization | OrganizationDTO
): Organization => {
  return {
    ...organization,
    createdAt: new Date(organization.createdAt),
    updatedAt: new Date(organization.updatedAt),
  };
};

export const mapTeamInvite = (
  teamInvite: TeamInvite | TeamInviteDTO
): TeamInvite => {
  return { ...teamInvite, createdAt: new Date(teamInvite.createdAt) };
};
