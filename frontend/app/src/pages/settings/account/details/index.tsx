import React from 'react';
import { AccountSettingsDetailsPage } from 'modules/settings/pages/account/AccountSettingsDetailsPage';
import {
  getAuthenticatedServerSideProps,
  AuthenticatedServerSideProps,
} from 'modules/auth/middleware/authMiddleware';
import type { GetServerSideProps } from 'next';

type Props = AuthenticatedServerSideProps;

export const AccountSettingsDetails = ({ user }: Props) => {
  return <AccountSettingsDetailsPage user={user} />;
};

export const getServerSideProps: GetServerSideProps<Props> = getAuthenticatedServerSideProps;

export default AccountSettingsDetails;
