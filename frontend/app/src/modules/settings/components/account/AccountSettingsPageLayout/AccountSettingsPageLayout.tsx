import React from 'react';
import { AppLayout } from 'modules/app/components/AppLayout';
import type { SettingsLayoutPropsBase } from 'modules/settings/types';
import { Organization, User } from '@rebrowse/types';
import Head from 'next/head';

import { AccountSettingsLayout } from '../AccountSettingsLayout';

type Props = SettingsLayoutPropsBase & {
  user: User;
  organization: Organization;
};

export const AccountSettingsPageLayout = ({
  user,
  organization,
  title,
  ...rest
}: Props) => {
  return (
    <AppLayout user={user} organization={organization}>
      <Head>
        <title>{title || rest.header} | Account settings</title>
      </Head>
      <AccountSettingsLayout {...rest} />
    </AppLayout>
  );
};
