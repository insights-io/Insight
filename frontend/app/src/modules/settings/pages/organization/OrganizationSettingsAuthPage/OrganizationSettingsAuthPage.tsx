import React from 'react';
import {
  SETTINGS_PATH_PART,
  ORGANIZATION_SETTINGS_PAGE_PART,
  ORGANIZATION_SETTINGS_AUTH_PAGE_PART,
} from 'shared/constants/routes';
import { OrganizationSettingsPageLayout } from 'modules/settings/components/organization/OrganizationSettingsPageLayout';
import { useSsoSetup } from 'modules/settings/hooks/useSsoSetup';
import { AuthenticationSetup } from 'modules/settings/components/organization/AuthenticationSetup';
import type { Path } from 'modules/settings/types';
import type { SsoSetupDTO } from '@insight/types';

const PATH: Path = [
  SETTINGS_PATH_PART,
  ORGANIZATION_SETTINGS_PAGE_PART,
  ORGANIZATION_SETTINGS_AUTH_PAGE_PART,
];

type Props = {
  maybeSsoSetup: SsoSetupDTO | undefined;
};

export const OrganizationSettingsAuthPage = ({
  maybeSsoSetup: initialMaybeSsoSetup,
}: Props) => {
  const { maybeSsoSetup, setSsoSetup } = useSsoSetup(initialMaybeSsoSetup);

  return (
    <OrganizationSettingsPageLayout path={PATH} header="Authentication">
      <AuthenticationSetup
        maybeSsoSetup={maybeSsoSetup}
        setSsoSetup={setSsoSetup}
      />
    </OrganizationSettingsPageLayout>
  );
};
