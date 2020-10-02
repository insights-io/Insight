import React from 'react';
import { AccountSettingsSecurityPage } from 'modules/settings/pages/account/AccountSettingsSecurityPage';
import {
  getAuthenticatedServerSideProps,
  AuthenticatedServerSideProps,
} from 'modules/auth/middleware/authMiddleware';
import type { GetServerSideProps } from 'next';

type Props = AuthenticatedServerSideProps;

export const AccountSettingsSecurity = ({ user, organization }: Props) => {
  return (
    <AccountSettingsSecurityPage user={user} organization={organization} />
  );
};

export const getServerSideProps: GetServerSideProps<Props> = getAuthenticatedServerSideProps;

export default AccountSettingsSecurity;
