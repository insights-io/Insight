import React from 'react';
import {
  SETTINGS_PATH_PART,
  ORGANIZATION_SETTINGS_PAGE_PART,
  ORGANIZATION_SETTINGS_SECURITY_AND_PRIVACY_PAGE_PART,
} from 'shared/constants/routes';
import { OrganizationSettingsPageLayout } from 'modules/settings/components/organization/OrganizationSettingsPageLayout';
import type { Path } from 'modules/settings/types';
import { OrganizationDTO, UserDTO } from '@insight/types';
import { useUser } from 'shared/hooks/useUser';
import { useOrganization } from 'shared/hooks/useOrganization';

const PATH: Path = [
  SETTINGS_PATH_PART,
  ORGANIZATION_SETTINGS_PAGE_PART,
  ORGANIZATION_SETTINGS_SECURITY_AND_PRIVACY_PAGE_PART,
];

type Props = {
  user: UserDTO;
  organization: OrganizationDTO;
};

export const OrganizationSettingsSecurityAndPrivacyPage = ({
  user: initialUser,
  organization: initialOrganization,
}: Props) => {
  const { user } = useUser(initialUser);
  const { organization } = useOrganization(initialOrganization);

  return (
    <OrganizationSettingsPageLayout
      path={PATH}
      header="Security & Privacy"
      user={user}
      organization={organization}
    >
      <div>Coming soon.</div>
    </OrganizationSettingsPageLayout>
  );
};
