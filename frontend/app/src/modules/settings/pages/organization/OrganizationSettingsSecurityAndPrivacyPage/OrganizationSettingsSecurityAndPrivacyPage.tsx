import React from 'react';
import {
  SETTINGS_PATH_PART,
  ORGANIZATION_SETTINGS_PAGE_PART,
  ORGANIZATION_SETTINGS_SECURITY_AND_PRIVACY_PAGE_PART,
} from 'shared/constants/routes';
import { OrganizationSettingsPageLayout } from 'modules/settings/components/organization/OrganizationSettingsPageLayout';
import type { Path } from 'modules/settings/types';

const PATH: Path = [
  SETTINGS_PATH_PART,
  ORGANIZATION_SETTINGS_PAGE_PART,
  ORGANIZATION_SETTINGS_SECURITY_AND_PRIVACY_PAGE_PART,
];

export const OrganizationSettingsSecurityAndPrivacyPage = () => {
  return (
    <OrganizationSettingsPageLayout path={PATH} header="Security & Privacy">
      <div>Coming soon.</div>
    </OrganizationSettingsPageLayout>
  );
};
