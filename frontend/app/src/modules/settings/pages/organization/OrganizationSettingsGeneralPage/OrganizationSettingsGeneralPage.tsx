import React from 'react';
import {
  SETTINGS_PATH_PART,
  ORGANIZATION_SETTINGS_PAGE_PART,
  ORGANIZATION_SETTINGS_GENERAL_PAGE_PART,
} from 'shared/constants/routes';
import { OrganizationSettingsPageLayout } from 'modules/settings/components/organization/OrganizationSettingsPageLayout';
import type { Path } from 'modules/settings/types';
import type { OrganizationDTO, UserDTO } from '@insight/types';
import { OrganizationInfoTable } from 'modules/settings/components/organization/OrganizationInfoTable';
import { useOrganization } from 'shared/hooks/useOrganization';
import { useUser } from 'shared/hooks/useUser';

const PATH: Path = [
  SETTINGS_PATH_PART,
  ORGANIZATION_SETTINGS_PAGE_PART,
  ORGANIZATION_SETTINGS_GENERAL_PAGE_PART,
];

type Props = {
  organization: OrganizationDTO;
  user: UserDTO;
};

export const OrganizationSettingsGeneralPage = ({
  organization: initialOrganization,
  user: initialUser,
}: Props) => {
  const { user } = useUser(initialUser);
  const { organization } = useOrganization(initialOrganization);

  return (
    <OrganizationSettingsPageLayout
      user={user}
      organization={organization}
      path={PATH}
      header="General"
    >
      <OrganizationInfoTable organization={organization} />
    </OrganizationSettingsPageLayout>
  );
};
