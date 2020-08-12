import React from 'react';
import { configureStory, mockApiError } from '@insight/storybook';
import AuthApi from 'api/auth';

import GetStarted from './GetStarted';

export default {
  title: 'components/GetStarted',
};

export const Base = () => {
  return <GetStarted />;
};
Base.story = configureStory({
  setupMocks: (sandbox) => {
    return sandbox
      .stub(AuthApi.signup, 'create')
      .resolves(new Promise((resolve) => setTimeout(resolve, 100)));
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
