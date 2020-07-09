import React from 'react';
import AccountSettings from 'modules/settings/components/AccountSettings';
import Router from 'next/router';
import { mapUser } from '@insight/sdk';
import { User } from '@insight/types';

type Props = {
  user: User;
};

const AccountOrganizationSettingsPage = ({ user }: Props) => {
  return (
    <AccountSettings
      activeKey="/account/organization-settings"
      onTabChange={(key) => Router.push(key)}
      user={mapUser(user)}
    />
  );
};

export default AccountOrganizationSettingsPage;
