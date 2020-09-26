import React from 'react';
import { INSIGHT_ADMIN } from 'test/data';
import { fullHeightDecorator } from '@insight/storybook';
import { Meta } from '@storybook/react';

import AccountApiKeysPage from './AccountApiKeysPage';

export default {
  title: 'settings/pages/AccountApiKeysPage',
  component: AccountApiKeysPage,
  decorators: [fullHeightDecorator],
} as Meta;

export const Base = () => {
  return <AccountApiKeysPage user={INSIGHT_ADMIN} />;
};
