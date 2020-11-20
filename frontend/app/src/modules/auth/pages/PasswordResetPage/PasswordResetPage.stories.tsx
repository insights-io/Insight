import React from 'react';
import { configureStory } from '@rebrowse/storybook';
import { AuthApi } from 'api';
import type { Meta } from '@storybook/react';

import { PasswordResetPage } from './PasswordResetPage';

export default {
  title: 'auth/pages/PasswordResetPage',
  component: PasswordResetPage,
} as Meta;

export const Base = () => {
  return <PasswordResetPage token="1234" />;
};
Base.story = configureStory({
  setupMocks: (sandbox) => {
    return sandbox.stub(AuthApi.password, 'reset').resolves();
  },
});
