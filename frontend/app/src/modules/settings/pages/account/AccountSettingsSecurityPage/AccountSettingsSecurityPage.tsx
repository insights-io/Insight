import React from 'react';
import { Block } from 'baseui/block';
import ChangePassword from 'modules/settings/components/account/ChangePassword';
import { TwoFactorAuthentication } from 'modules/settings/components/account/TwoFactorAuthentication';
import { AccountSettingsPageLayout } from 'modules/settings/components/account/AccountSettingsPageLayout';
import {
  SETTINGS_PATH_PART,
  ACCOUNT_SETTINGS_SECURITY_PAGE_PART,
  ACCOUNT_SETTINGS_PATH_PART,
} from 'shared/constants/routes';
import type { UserDTO } from '@insight/types';
import type { Path } from 'modules/settings/types';
import { useUser } from 'shared/hooks/useUser';

const PATH: Path = [
  SETTINGS_PATH_PART,
  ACCOUNT_SETTINGS_PATH_PART,
  ACCOUNT_SETTINGS_SECURITY_PAGE_PART,
];

type Props = {
  user: UserDTO;
};

export const AccountSettingsSecurityPage = ({ user: initialUser }: Props) => {
  const { user } = useUser(initialUser);

  return (
    <AccountSettingsPageLayout user={user} path={PATH} header="Security">
      <TwoFactorAuthentication user={user} />
      <Block marginTop="24px">
        <ChangePassword />
      </Block>
    </AccountSettingsPageLayout>
  );
};
