import React from 'react';
import {
  AuthenticatedServerSideProps,
  getAuthenticatedServerSideProps,
} from 'auth/middleware/authMiddleware';
import { SettingsPage } from 'settings/pages/SettingsPage';
import type { GetServerSideProps } from 'next';
import type { OrganizationDTO } from '@rebrowse/types';

type Props = AuthenticatedServerSideProps & {
  organization: OrganizationDTO;
};

const Settings = ({ user, organization }: Props) => {
  return <SettingsPage user={user} organization={organization} />;
};

export const getServerSideProps: GetServerSideProps<Props> = getAuthenticatedServerSideProps;

export default Settings;
