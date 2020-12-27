import React from 'react';
import { AccountSettingsDetailsPage } from 'settings/pages/account/AccountSettingsDetailsPage';
import {
  getAuthenticatedServerSideProps,
  AuthenticatedServerSideProps,
} from 'auth/middleware/authMiddleware';
import type { GetServerSideProps } from 'next';

type Props = AuthenticatedServerSideProps;

export const AccountSettingsDetails = ({ user, organization }: Props) => {
  return <AccountSettingsDetailsPage user={user} organization={organization} />;
};

export const getServerSideProps: GetServerSideProps<Props> = getAuthenticatedServerSideProps;

export default AccountSettingsDetails;
