import type {
  Organization,
  OrganizationDTO,
  OrganizationPasswordPolicy,
  OrganizationPasswordPolicyDTO,
  TeamInvite,
  TeamInviteDTO,
} from '@rebrowse/types';

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
  return {
    ...teamInvite,
    createdAt: new Date(teamInvite.createdAt),
    expiresAt: new Date(teamInvite.expiresAt),
  };
};

export const mapPasswordPolicy = (
  policy: OrganizationPasswordPolicy | OrganizationPasswordPolicyDTO
): OrganizationPasswordPolicy => {
  return {
    ...policy,
    createdAt: new Date(policy.createdAt),
    updatedAt: new Date(policy.updatedAt),
  };
};
