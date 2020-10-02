import React from 'react';
import { AppLayout } from 'modules/app/components/AppLayout';
import type { SettingsLayoutPropsBase } from 'modules/settings/types';
import { Organization, User } from '@insight/types';

import { OrganizationSettingsLayout } from '../OrganizationSettingsLayout';

type Props = SettingsLayoutPropsBase & {
  user: User;
  organization: Organization;
};

export const OrganizationSettingsPageLayout = ({
  user,
  organization,
  ...rest
}: Props) => {
  return (
    <AppLayout user={user} organization={organization}>
      <OrganizationSettingsLayout {...rest} />
    </AppLayout>
  );
};
