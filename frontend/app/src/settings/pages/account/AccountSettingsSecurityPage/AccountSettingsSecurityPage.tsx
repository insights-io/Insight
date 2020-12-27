import React from 'react';
import { Block } from 'baseui/block';
import { ChangePassword } from 'settings/components/account/ChangePassword';
import { AccountMfaPanel } from 'settings/components/account/AccountMfaPanel';
import { AccountSettingsPageLayout } from 'settings/components/account/AccountSettingsPageLayout';
import {
  SETTINGS_PATH_PART,
  ACCOUNT_SETTINGS_SECURITY_PAGE_PART,
  ACCOUNT_SETTINGS_PATH_PART,
} from 'shared/constants/routes';
import type { MfaSetupDTO, OrganizationDTO, UserDTO } from '@rebrowse/types';
import type { Path } from 'settings/types';
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
  mfaSetups: MfaSetupDTO[];
};

export const AccountSettingsSecurityPage = ({
  user: initialUser,
  organization: initialOrganization,
  mfaSetups: initialMfaSetups,
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
      <AccountMfaPanel user={user} mfaSetups={initialMfaSetups} />
      <Block marginTop="24px">
        <ChangePassword />
      </Block>
    </AccountSettingsPageLayout>
  );
};
