import React from 'react';
import AccountSettings from 'modules/settings/AccountSettings';
import Router from 'next/router';
import { GetServerSideProps } from 'next';
import {
  getServerSideAuthProps,
  AuthMiddlewareProps,
} from 'modules/auth/middleware/authMiddleware';

type Props = AuthMiddlewareProps;

const Settings = ({ user }: Props) => {
  return (
    <AccountSettings
      activeKey="/account/settings"
      onTabChange={(key) => Router.push(key)}
      initialUser={user}
    />
  );
};

export const getServerSideProps: GetServerSideProps<Props> = getServerSideAuthProps;

export default Settings;
