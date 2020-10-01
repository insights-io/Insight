import React from 'react';
import AppLayout from 'modules/app/components/AppLayout';
import type { SettingsLayoutPropsBase } from 'modules/settings/types';
import { User } from '@insight/types';

import { OrganizationSettingsLayout } from '../OrganizationSettingsLayout';

type Props = SettingsLayoutPropsBase & {
  user: User;
};

export const OrganizationSettingsPageLayout = ({ user, ...rest }: Props) => {
  return (
    <AppLayout user={user}>
      <OrganizationSettingsLayout {...rest} />
    </AppLayout>
  );
};
