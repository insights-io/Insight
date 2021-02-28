import React from 'react';
import type { Meta } from '@storybook/react';
import { configureStory } from '@rebrowse/storybook';
import { client } from 'sdk';
import { appBaseUrl } from 'shared/config';
import type { SinonSandbox } from 'sinon';

import { PasswordForgotPage } from './PasswordForgotPage';

export default {
  title: 'password/pages/PasswordForgotPage',
  component: PasswordForgotPage,
} as Meta;

const setupMocks = (sandbox: SinonSandbox) => {
  return sandbox.stub(client.password, 'forgot').resolves();
};

export const Base = () => {
  return <PasswordForgotPage redirect={appBaseUrl} />;
};
Base.story = configureStory({
  setupMocks,
});
