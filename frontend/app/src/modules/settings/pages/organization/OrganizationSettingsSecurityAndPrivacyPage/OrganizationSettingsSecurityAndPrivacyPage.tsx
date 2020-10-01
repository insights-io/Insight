import React from 'react';
import {
  SETTINGS_PATH_PART,
  ORGANIZATION_SETTINGS_PAGE_PART,
  ORGANIZATION_SETTINGS_SECURITY_AND_PRIVACY_PAGE_PART,
} from 'shared/constants/routes';
import { OrganizationSettingsPageLayout } from 'modules/settings/components/organization/OrganizationSettingsPageLayout';
import type { Path } from 'modules/settings/types';
import { UserDTO } from '@insight/types';
import { useUser } from 'shared/hooks/useUser';

const PATH: Path = [
  SETTINGS_PATH_PART,
  ORGANIZATION_SETTINGS_PAGE_PART,
  ORGANIZATION_SETTINGS_SECURITY_AND_PRIVACY_PAGE_PART,
];

type Props = {
  user: UserDTO;
};

export const OrganizationSettingsSecurityAndPrivacyPage = ({
  user: initialUser,
}: Props) => {
  const { user } = useUser(initialUser);
  return (
    <OrganizationSettingsPageLayout
      path={PATH}
      header="Security & Privacy"
      user={user}
    >
      <div>Coming soon.</div>
    </OrganizationSettingsPageLayout>
  );
};
