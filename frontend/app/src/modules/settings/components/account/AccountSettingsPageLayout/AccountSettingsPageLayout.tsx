import React from 'react';
import AppLayout from 'modules/app/components/AppLayout';
import type { SettingsLayoutPropsBase } from 'modules/settings/types';
import { User } from '@insight/types';

import { AccountSettingsLayout } from '../AccountSettingsLayout';

type Props = SettingsLayoutPropsBase & {
  user: User;
};

export const AccountSettingsPageLayout = ({ user, ...rest }: Props) => {
  return (
    <AppLayout user={user}>
      <AccountSettingsLayout {...rest} />
    </AppLayout>
  );
};
