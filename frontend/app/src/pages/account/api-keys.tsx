import React from 'react';
import authenticated, { WithAuthProps } from 'modules/auth/hoc/authenticated';
import AccountSettings from 'modules/settings/AccountSettings';
import Router from 'next/router';

type Props = WithAuthProps;

const APIKeysSettings = ({ user }: Props) => {
  return (
    <AccountSettings
      activeKey="/account/api-keys"
      onTabChange={(key) => Router.push(key)}
      initialUser={user}
    />
  );
};

export default authenticated(APIKeysSettings);
