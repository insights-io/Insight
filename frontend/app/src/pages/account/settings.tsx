import React from 'react';
import AccountSettings from 'modules/settings/AccountSettings';
import Router from 'next/router';
import { GetServerSideProps } from 'next';
import {
  AuthenticatedServerSideProps,
  getAuthenticatedServerSideProps,
} from 'modules/auth/middleware/authMiddleware';

type Props = AuthenticatedServerSideProps;

const Settings = ({ user }: Props) => {
  return (
    <AccountSettings
      activeKey="/account/settings"
      onTabChange={(key) => Router.push(key)}
      initialUser={user}
    />
  );
};

export const getServerSideProps: GetServerSideProps<Props> = getAuthenticatedServerSideProps;

export default Settings;
