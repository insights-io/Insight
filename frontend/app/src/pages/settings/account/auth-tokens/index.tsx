import React from 'react';
import { AccountSettingsAuthTokensPage } from 'modules/settings/pages/account/AccountSettingsAuthTokensPage';
import {
  getAuthenticatedServerSideProps,
  AuthenticatedServerSideProps,
} from 'modules/auth/middleware/authMiddleware';
import type { GetServerSideProps } from 'next';

type Props = AuthenticatedServerSideProps;

export const AccountSettingsAuthTokens = (_props: Props) => {
  return <AccountSettingsAuthTokensPage />;
};

export const getServerSideProps: GetServerSideProps<Props> = getAuthenticatedServerSideProps;

export default AccountSettingsAuthTokens;
