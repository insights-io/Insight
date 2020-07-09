import React from 'react';
import { INSIGHT_ADMIN } from 'test/data';
import { fullHeightDecorator } from '@insight/storybook';

import AccountOrganizationSettingsPage from './AccountOrganizationSettingsPage';

export default {
  title: 'settings|pages/AccountOrganizationSettingsPage',
  decorators: [fullHeightDecorator],
};

export const Base = () => {
  return <AccountOrganizationSettingsPage user={INSIGHT_ADMIN} />;
};
