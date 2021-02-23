import React from 'react';
import { configureStory, mockApiError } from '@rebrowse/storybook';
import type { Meta } from '@storybook/react';
import { client } from 'sdk';
import { appBaseUrl } from 'shared/config';

import { SignUpPage } from './SignUpPage';

export default {
  title: 'signup/pages/SignUpPage',
  component: SignUpPage,
} as Meta;

export const Base = () => {
  return <SignUpPage redirect={appBaseUrl} />;
};
Base.story = configureStory({
  setupMocks: (sandbox) => {
    return sandbox
      .stub(client.signup, 'create')
      .resolves({ statusCode: 200, headers: new Headers() });
  },
});

export const WithError = () => {
  return <SignUpPage redirect={appBaseUrl} />;
};
WithError.story = configureStory({
  setupMocks: (sandbox) => {
    return sandbox.stub(client.signup, 'create').rejects(
      mockApiError({
        statusCode: 400,
        reason: 'Bad Request',
        message: 'Something went wrong',
      })
    );
  },
});
