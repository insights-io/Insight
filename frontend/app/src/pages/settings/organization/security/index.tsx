import React from 'react';
import { GetServerSideProps } from 'next';
import {
  AuthenticatedServerSideProps,
  getAuthenticatedServerSideProps,
} from 'modules/auth/middleware/authMiddleware';
import { mapUser } from '@insight/sdk';
import AccountOrganizationSettingsPage from 'modules/settings/pages/AccountOrganizationSettingsPage';
import { ORGANIZATION_SECURITY_SETTINGS_PAGE } from 'shared/constants/routes';

type Props = AuthenticatedServerSideProps;

const SecurityOrganizationSettings = ({ user }: Props) => {
  return (
    <AccountOrganizationSettingsPage
      user={mapUser(user)}
      activeTab={ORGANIZATION_SECURITY_SETTINGS_PAGE}
    />
  );
};

export const getServerSideProps: GetServerSideProps<Props> = getAuthenticatedServerSideProps;

export default SecurityOrganizationSettings;