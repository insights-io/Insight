import React from 'react';
import { AppLayout } from 'modules/app/components/AppLayout';
import type { SettingsLayoutPropsBase } from 'modules/settings/types';
import { Organization, User } from '@insight/types';

import { AccountSettingsLayout } from '../AccountSettingsLayout';

type Props = SettingsLayoutPropsBase & {
  user: User;
  organization: Organization;
};

export const AccountSettingsPageLayout = ({
  user,
  organization,
  ...rest
}: Props) => {
  return (
    <AppLayout user={user} organization={organization}>
      <AccountSettingsLayout {...rest} />
    </AppLayout>
  );
};
