import { mapOrganization } from '@rebrowse/sdk';
import type { Organization, OrganizationDTO } from '@rebrowse/types';

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
