import React from 'react';
import { configureStory } from '@rebrowse/storybook';
import type { Meta } from '@storybook/react';
import { mockPasswordForgotPage as setupMocks } from '__tests__/mocks';

import { PasswordForgotPage } from './PasswordForgotPage';

export default {
  title: 'auth/pages/PasswordForgotPage',
  component: PasswordForgotPage,
} as Meta;

export const Base = () => {
  return <PasswordForgotPage />;
};
Base.story = configureStory({ setupMocks });
