import React from 'react';
import Router from 'next/router';
import { User } from '@insight/types';
import AccountSettings from 'modules/settings/components/AccountSettings';
import { USER_SETTINGS_PAGE } from 'shared/constants/routes';

type Props = {
  user: User;
};

const AccountSettingsPage = ({ user }: Props) => {
  return (
    <AccountSettings
      activeTab={USER_SETTINGS_PAGE}
      onTabChange={(key) => Router.push(key)}
      user={user}
    />
  );
};

export default AccountSettingsPage;
