import React from 'react';
import { configureStory } from '@rebrowse/storybook';
import type { Meta } from '@storybook/react';
import { mockPasswordResetPage as setupMocks } from '__tests__/mocks';

import { PasswordResetPage } from './PasswordResetPage';

export default {
  title: 'auth/pages/PasswordResetPage',
  component: PasswordResetPage,
} as Meta;

export const Base = () => {
  return <PasswordResetPage token="1234" />;
};
Base.story = configureStory({ setupMocks });
