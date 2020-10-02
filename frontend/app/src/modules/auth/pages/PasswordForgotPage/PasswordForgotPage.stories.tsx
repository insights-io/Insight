import React from 'react';
import { configureStory } from '@insight/storybook';
import { AuthApi } from 'api';
import type { Meta } from '@storybook/react';

import { PasswordForgotPage } from './PasswordForgotPage';

export default {
  title: 'auth/pages/PasswordForgotPage',
  component: PasswordForgotPage,
} as Meta;

export const Base = () => {
  return <PasswordForgotPage />;
};
Base.story = configureStory({
  setupMocks: (sandbox) => {
    return sandbox.stub(AuthApi.password, 'forgot').resolves({ data: true });
  },
});
