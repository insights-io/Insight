import React from 'react';
import { GetServerSideProps } from 'next';
import {
  AuthenticatedServerSideProps,
  getAuthenticatedServerSideProps,
} from 'modules/auth/middleware/authMiddleware';

import GeneralOrganizationSettings from './general';

type Props = AuthenticatedServerSideProps;

const AccountOrganizationSettings = ({ user }: Props) => {
  return <GeneralOrganizationSettings user={user} />;
};

export const getServerSideProps: GetServerSideProps<Props> = getAuthenticatedServerSideProps;

export default AccountOrganizationSettings;
