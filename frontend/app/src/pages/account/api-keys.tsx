import React from 'react';
import { GetServerSideProps } from 'next';
import {
  AuthenticatedServerSideProps,
  getAuthenticatedServerSideProps,
} from 'modules/auth/middleware/authMiddleware';
import AccountApiKeysPage from 'modules/settings/pages/AccountApiKeysPage';
import { mapUser } from '@insight/sdk';

type Props = AuthenticatedServerSideProps;

const AccountApiKeys = ({ user }: Props) => {
  return <AccountApiKeysPage user={mapUser(user)} />;
};

export const getServerSideProps: GetServerSideProps<Props> = getAuthenticatedServerSideProps;

export default AccountApiKeys;
