import React from 'react';
import { configureStory, mockApiError } from '@insight/storybook';
import SsoApi from 'api/sso';

import Login from './Login';

export default {
  title: 'Auth|Login',
};

export const Base = () => {
  return <Login />;
};
Base.story = configureStory({
  setupMocks: (sandbox) => {
    return sandbox.stub(SsoApi, 'login').callsFake(() => {
      return new Promise((resolve) => setTimeout(resolve, 10));
    });
  },
});

export const InvalidPassword = () => {
  return <Login />;
};
InvalidPassword.story = configureStory({
  setupMocks: (sandbox) => {
    return sandbox.stub(SsoApi, 'login').callsFake(() => {
      const error = mockApiError({
        statusCode: 400,
        reason: 'Bad Request',
        message: 'Invalid email or password',
      });

      return new Promise((_resolve, reject) =>
        setTimeout(() => reject(error), 10)
      );
    });
  },
});
