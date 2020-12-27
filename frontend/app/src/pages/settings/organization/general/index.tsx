import React from 'react';
import type { GetServerSideProps } from 'next';
import {
  AuthenticatedServerSideProps,
  getAuthenticatedServerSideProps,
} from 'auth/middleware/authMiddleware';
import { OrganizationSettingsGeneralPage } from 'settings/pages/organization/OrganizationSettingsGeneralPage';
import type { OrganizationDTO } from '@rebrowse/types';

type Props = AuthenticatedServerSideProps & {
  organization: OrganizationDTO;
};

export const OrganizationSettingsGeneral = ({ organization, user }: Props) => {
  return (
    <OrganizationSettingsGeneralPage organization={organization} user={user} />
  );
};

export const getServerSideProps: GetServerSideProps<Props> = getAuthenticatedServerSideProps;

export default OrganizationSettingsGeneral;
