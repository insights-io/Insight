import React from 'react';
import Router from 'next/router';
import { User } from '@insight/types';
import AccountSettings from 'modules/settings/components/AccountSettings';

type Props = {
  user: User;
};

const AccountSettingsPage = ({ user }: Props) => {
  return (
    <AccountSettings
      activeKey="/account/settings"
      onTabChange={(key) => Router.push(key)}
      user={user}
    />
  );
};

export default AccountSettingsPage;
