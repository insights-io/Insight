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
import type { SsoSetupDTO, UserDTO } from '@insight/types';
import { useUser } from 'shared/hooks/useUser';

const PATH: Path = [
  SETTINGS_PATH_PART,
  ORGANIZATION_SETTINGS_PAGE_PART,
  ORGANIZATION_SETTINGS_AUTH_PAGE_PART,
];

type Props = {
  maybeSsoSetup: SsoSetupDTO | undefined;
  user: UserDTO;
};

export const OrganizationSettingsAuthPage = ({
  maybeSsoSetup: initialMaybeSsoSetup,
  user: initialUser,
}: Props) => {
  const { maybeSsoSetup, setSsoSetup } = useSsoSetup(initialMaybeSsoSetup);
  const { user } = useUser(initialUser);

  return (
    <OrganizationSettingsPageLayout
      path={PATH}
      header="Authentication"
      user={user}
    >
      <AuthenticationSetup
        maybeSsoSetup={maybeSsoSetup}
        setSsoSetup={setSsoSetup}
      />
    </OrganizationSettingsPageLayout>
  );
};
