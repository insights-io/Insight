import React from 'react';
import AccountSettings from 'modules/settings/components/AccountSettings';
import Router from 'next/router';
import { User } from '@insight/types';

type Props = {
  user: User;
};

const AccountApiKeysPage = ({ user }: Props) => {
  return (
    <AccountSettings
      activeKey="/account/api-keys"
      onTabChange={(key) => Router.push(key)}
      user={user}
    />
  );
};

export default AccountApiKeysPage;
