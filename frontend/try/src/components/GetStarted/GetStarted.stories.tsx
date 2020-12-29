import React from 'react';
import { configureStory, mockApiError } from '@rebrowse/storybook';
import { AuthApi } from 'api/auth';
import { Meta } from '@storybook/react';

import { GetStarted } from './GetStarted';

export default {
  title: 'components/GetStarted',
  component: GetStarted,
} as Meta;

export const Base = () => {
  return <GetStarted />;
};
Base.story = configureStory({
  setupMocks: (sandbox) => {
    return sandbox
      .stub(AuthApi.signup, 'create')
      .resolves({ statusCode: 200, headers: new Headers() });
  },
});

export const WithError = () => {
  return <GetStarted />;
};
WithError.story = configureStory({
  setupMocks: (sandbox) => {
    return sandbox.stub(AuthApi.signup, 'create').rejects(
      mockApiError({
        statusCode: 400,
        reason: 'Bad Request',
        message: 'Something went wrong',
      })
    );
  },
});
