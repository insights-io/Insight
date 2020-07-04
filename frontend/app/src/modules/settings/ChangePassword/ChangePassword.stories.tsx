import React from 'react';
import { configureStory, mockApiError } from '@insight/storybook';
import PasswordApi from 'api/password';

import ChangePassword from './ChangePassword';

export default {
  title: 'settings|ChangePassword',
};

export const Base = () => {
  return <ChangePassword />;
};
Base.story = configureStory({
  setupMocks: (sandbox) => {
    return sandbox.stub(PasswordApi, 'change').resolves({ data: true });
  },
});

export const WithError = () => {
  return <ChangePassword />;
};
WithError.story = configureStory({
  setupMocks: (sandbox) => {
    return sandbox.stub(PasswordApi, 'change').rejects(
      mockApiError({
        statusCode: 400,
        reason: 'Bad Request',
        message: 'New password cannot be the same as the previous one!',
      })
    );
  },
});
