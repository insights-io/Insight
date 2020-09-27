import React from 'react';
import {
  SETTINGS_PATH_PART,
  ORGANIZATION_SETTINGS_PAGE_PART,
  ORGANIZATION_SETTINGS_GENERAL_PAGE_PART,
} from 'shared/constants/routes';
import { OrganizationSettingsPageLayout } from 'modules/settings/components/organization/OrganizationSettingsPageLayout';
import type { Path } from 'modules/settings/types';
import type { OrganizationDTO } from '@insight/types';
import { OrganizationInfoTable } from 'modules/settings/components/organization/OrganizationInfoTable';
import useOrganization from 'shared/hooks/useOrganization';

const PATH: Path = [
  SETTINGS_PATH_PART,
  ORGANIZATION_SETTINGS_PAGE_PART,
  ORGANIZATION_SETTINGS_GENERAL_PAGE_PART,
];

type Props = {
  organization: OrganizationDTO;
};

export const OrganizationSettingsGeneralPage = ({
  organization: initialOrganization,
}: Props) => {
  const { organization } = useOrganization(initialOrganization);

  return (
    <OrganizationSettingsPageLayout path={PATH} header="General">
      <OrganizationInfoTable organization={organization} />
    </OrganizationSettingsPageLayout>
  );
};
