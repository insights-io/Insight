import React from 'react';
import type { Meta } from '@storybook/react';
import { configureStory } from '@rebrowse/storybook';
import { client } from 'sdk';

import { PasswordForgotPage } from './PasswordForgotPage';

export default {
  title: 'password/pages/PasswordForgotPage',
  component: PasswordForgotPage,
} as Meta;

export const Base = () => {
  return <PasswordForgotPage />;
};
Base.story = configureStory({
  setupMocks: (sandbox) => {
    return sandbox.stub(client.password, 'forgot').resolves();
  },
});
