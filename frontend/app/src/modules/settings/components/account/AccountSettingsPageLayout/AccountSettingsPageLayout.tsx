import React from 'react';
import AppLayout from 'modules/app/components/AppLayout';
import type { SettingsLayoutPropsBase } from 'modules/settings/types';

import { AccountSettingsLayout } from '../AccountSettingsLayout';

type Props = SettingsLayoutPropsBase;

export const AccountSettingsPageLayout = (props: Props) => {
  return (
    <AppLayout>
      <AccountSettingsLayout {...props} />
    </AppLayout>
  );
};
