import React from 'react';
import type { GetServerSideProps } from 'next';
import {
  AuthenticatedServerSideProps,
  getAuthenticatedServerSideProps,
} from 'modules/auth/middleware/authMiddleware';
import { OrganizationSettingsSecurityAndPrivacyPage } from 'modules/settings/pages/organization/OrganizationSettingsSecurityAndPrivacyPage';
import type { OrganizationDTO } from '@insight/types';

type Props = AuthenticatedServerSideProps & {
  organization: OrganizationDTO;
};

export const OrganizationSettingsSecurityAndPrivacy = ({
  user,
  organization,
}: Props) => {
  return (
    <OrganizationSettingsSecurityAndPrivacyPage
      user={user}
      organization={organization}
    />
  );
};

export const getServerSideProps: GetServerSideProps<Props> = getAuthenticatedServerSideProps;

export default OrganizationSettingsSecurityAndPrivacy;
