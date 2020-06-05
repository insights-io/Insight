import React from 'react';
import { configureStory, mockApiError } from '@insight/storybook';
import { SignupApi } from 'api/signup';

import GetStarted from './GetStarted';

export default {
  title: 'GetStarted',
};

export const Base = () => {
  return <GetStarted />;
};
Base.story = configureStory({
  setupMocks: (sandbox) => {
    return sandbox
      .stub(SignupApi, 'signup')
      .resolves(new Promise((resolve) => setTimeout(resolve, 100)));
  },
});

export const WithError = () => {
  return <GetStarted />;
};
WithError.story = configureStory({
  setupMocks: (sandbox) => {
    return sandbox.stub(SignupApi, 'signup').rejects(
      mockApiError({
        statusCode: 400,
        reason: 'Bad Request',
        message: 'Something went wrong',
      })
    );
  },
});
