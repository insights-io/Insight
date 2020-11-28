import React from 'react';
import { Block } from 'baseui/block';
import { ChangePassword } from 'modules/settings/components/account/ChangePassword';
import { AccountMfaPanel } from 'modules/settings/components/account/AccountMfaPanel';
import { AccountSettingsPageLayout } from 'modules/settings/components/account/AccountSettingsPageLayout';
import {
  SETTINGS_PATH_PART,
  ACCOUNT_SETTINGS_SECURITY_PAGE_PART,
  ACCOUNT_SETTINGS_PATH_PART,
} from 'shared/constants/routes';
import type { OrganizationDTO, UserDTO } from '@rebrowse/types';
import type { Path } from 'modules/settings/types';
import { useUser } from 'shared/hooks/useUser';
import { useOrganization } from 'shared/hooks/useOrganization';

const PATH: Path = [
  SETTINGS_PATH_PART,
  ACCOUNT_SETTINGS_PATH_PART,
  ACCOUNT_SETTINGS_SECURITY_PAGE_PART,
];

type Props = {
  user: UserDTO;
  organization: OrganizationDTO;
};

export const AccountSettingsSecurityPage = ({
  user: initialUser,
  organization: initialOrganization,
}: Props) => {
  const { user } = useUser(initialUser);
  const { organization } = useOrganization(initialOrganization);

  return (
    <AccountSettingsPageLayout
      user={user}
      organization={organization}
      path={PATH}
      header="Security"
    >
      <AccountMfaPanel user={user} />
      <Block marginTop="24px">
        <ChangePassword />
      </Block>
    </AccountSettingsPageLayout>
  );
};
