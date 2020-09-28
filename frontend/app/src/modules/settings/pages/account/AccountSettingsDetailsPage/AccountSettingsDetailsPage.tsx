import React from 'react';
import { AccountSettingsPageLayout } from 'modules/settings/components/account/AccountSettingsPageLayout';
import {
  ACCOUNT_SETTINGS_DETAILS_PAGE_PART,
  SETTINGS_PATH_PART,
  ACCOUNT_SETTINGS_PATH_PART,
} from 'shared/constants/routes';
import { AccountInfoTable } from 'modules/settings/components/account/AccountInfoTable';
import { useUser } from 'shared/hooks/useUser';
import type { Path } from 'modules/settings/types';
import type { UserDTO } from '@insight/types';

const PATH: Path = [
  SETTINGS_PATH_PART,
  ACCOUNT_SETTINGS_PATH_PART,
  ACCOUNT_SETTINGS_DETAILS_PAGE_PART,
];

type Props = {
  user: UserDTO;
};

export const AccountSettingsDetailsPage = ({ user: initialUser }: Props) => {
  const { user, updateUser, setUser } = useUser(initialUser);

  return (
    <AccountSettingsPageLayout path={PATH} header="Account details">
      <AccountInfoTable user={user} updateUser={updateUser} setUser={setUser} />
    </AccountSettingsPageLayout>
  );
};
