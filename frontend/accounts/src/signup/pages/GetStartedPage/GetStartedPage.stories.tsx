import React from 'react';
import { configureStory, mockApiError } from '@rebrowse/storybook';
import type { Meta } from '@storybook/react';
import { sdk } from 'api';

import { GetStartedPage } from './GetStartedPage';

export default {
  title: 'signup/pages/GetStartedPage',
  component: GetStartedPage,
} as Meta;

export const Base = () => {
  return <GetStartedPage />;
};
Base.story = configureStory({
  setupMocks: (sandbox) => {
    return sandbox
      .stub(sdk.signup, 'create')
      .resolves({ statusCode: 200, headers: new Headers() });
  },
});

export const WithError = () => {
  return <GetStartedPage />;
};
WithError.story = configureStory({
  setupMocks: (sandbox) => {
    return sandbox.stub(sdk.signup, 'create').rejects(
      mockApiError({
        statusCode: 400,
        reason: 'Bad Request',
        message: 'Something went wrong',
      })
    );
  },
});
