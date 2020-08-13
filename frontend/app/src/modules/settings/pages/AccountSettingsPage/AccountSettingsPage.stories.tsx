import React from 'react';
import { INSIGHT_ADMIN } from 'test/data';
import { fullHeightDecorator } from '@insight/storybook';

import AccountSettingsPage from './AccountSettingsPage';

export default {
  title: 'settings/pages/AccountSettingsPage',
  decorators: [fullHeightDecorator],
};

export const Base = () => {
  return <AccountSettingsPage user={INSIGHT_ADMIN} />;
};
