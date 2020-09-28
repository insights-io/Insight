import React from 'react';
import AppLayout from 'modules/app/components/AppLayout';
import type { SettingsLayoutPropsBase } from 'modules/settings/types';

import { OrganizationSettingsLayout } from '../OrganizationSettingsLayout';

type Props = SettingsLayoutPropsBase;

export const OrganizationSettingsPageLayout = (props: Props) => {
  return (
    <AppLayout>
      <OrganizationSettingsLayout {...props} />
    </AppLayout>
  );
};
