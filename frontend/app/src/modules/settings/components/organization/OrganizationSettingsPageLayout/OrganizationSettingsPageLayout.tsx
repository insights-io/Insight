import React from 'react';
import { AppLayout } from 'modules/app/components/AppLayout';
import type { SettingsLayoutPropsBase } from 'modules/settings/types';
import { Organization, User } from '@insight/types';
import Head from 'next/head';

import { OrganizationSettingsLayout } from '../OrganizationSettingsLayout';

type Props = SettingsLayoutPropsBase & {
  user: User;
  organization: Organization;
};

export const OrganizationSettingsPageLayout = ({
  user,
  organization,
  title,
  ...rest
}: Props) => {
  return (
    <AppLayout user={user} organization={organization}>
      <Head>
        <title>{title || rest.header} | Organization settings</title>
      </Head>
      <OrganizationSettingsLayout {...rest} />
    </AppLayout>
  );
};
