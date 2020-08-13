import React from 'react';
import { INSIGHT_ADMIN } from 'test/data';
import { fullHeightDecorator } from '@insight/storybook';

import AccountApiKeysPage from './AccountApiKeysPage';

export default {
  title: 'settings/pages/AccountApiKeysPage',
  decorators: [fullHeightDecorator],
};

export const Base = () => {
  return <AccountApiKeysPage user={INSIGHT_ADMIN} />;
};
