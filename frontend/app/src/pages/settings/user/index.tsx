import React from 'react';
import { GetServerSideProps } from 'next';
import {
  AuthenticatedServerSideProps,
  getAuthenticatedServerSideProps,
} from 'modules/auth/middleware/authMiddleware';
import AccountSettingsPage from 'modules/settings/pages/AccountSettingsPage';
import { mapUser } from '@insight/sdk';

type Props = AuthenticatedServerSideProps;

const AccountSettings = ({ user }: Props) => {
  return <AccountSettingsPage user={mapUser(user)} />;
};

export const getServerSideProps: GetServerSideProps<Props> = getAuthenticatedServerSideProps;

export default AccountSettings;
