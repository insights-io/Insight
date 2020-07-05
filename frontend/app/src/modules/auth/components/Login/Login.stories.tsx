import React from 'react';
import { configureStory, mockApiError } from '@insight/storybook';
import AuthApi from 'api/auth';
import { ResponsePromise } from 'ky';

import Login from './Login';

export default {
  title: 'Auth|Login',
};

export const Base = () => {
  return <Login />;
};
Base.story = configureStory({
  setupMocks: (sandbox) => {
    return sandbox.stub(AuthApi.sso, 'login').callsFake(() => {
      return new Promise((resolve) =>
        setTimeout(resolve, 10)
      ) as ResponsePromise;
    });
  },
});

export const InvalidPassword = () => {
  return <Login />;
};
InvalidPassword.story = configureStory({
  setupMocks: (sandbox) => {
    return sandbox.stub(AuthApi.sso, 'login').callsFake(() => {
      const error = mockApiError({
        statusCode: 400,
        reason: 'Bad Request',
        message: 'Invalid email or password',
      });

      return new Promise((_resolve, reject) =>
        setTimeout(() => reject(error), 10)
      ) as ResponsePromise;
    });
  },
});
