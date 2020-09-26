import React from 'react';
import AccountSettings from 'modules/settings/components/AccountSettings';
import Router from 'next/router';
import { mapUser } from '@insight/sdk';
import { User } from '@insight/types';

type Props = {
  user: User;
  activeTab: string;
};

const AccountOrganizationSettingsPage = ({ user, activeTab }: Props) => {
  return (
    <AccountSettings
      activeTab={activeTab}
      onTabChange={(key) => Router.push(key)}
      user={mapUser(user)}
    />
  );
};

export default AccountOrganizationSettingsPage;
