import React from 'react';
import authenticated, { WithAuthProps } from 'modules/auth/hoc/authenticated';
import AccountSettings from 'modules/settings/AccountSettings';
import Router from 'next/router';

type Props = WithAuthProps;

const OrganizationSettings = ({ user }: Props) => {
  console.log(user);
  return (
    <AccountSettings
      activeKey="/account/organization-settings"
      onTabChange={(key) => Router.push(key)}
      initialUser={user}
    />
  );
};

export default authenticated(OrganizationSettings);
