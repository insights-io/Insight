import React from 'react';
import { GetServerSideProps } from 'next';
import {
  AuthenticatedServerSideProps,
  getAuthenticatedServerSideProps,
} from 'modules/auth/middleware/authMiddleware';
import { mapUser } from '@insight/sdk';
import AccountOrganizationSettingsPage from 'modules/settings/pages/AccountOrganizationSettingsPage';

type Props = AuthenticatedServerSideProps;

const AccountOrganizationSettings = ({ user }: Props) => {
  return <AccountOrganizationSettingsPage user={mapUser(user)} />;
};

export const getServerSideProps: GetServerSideProps<Props> = getAuthenticatedServerSideProps;

export default AccountOrganizationSettings;
