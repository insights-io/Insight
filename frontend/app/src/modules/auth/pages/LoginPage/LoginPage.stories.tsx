import React from 'react';
import { configureStory, mockApiError } from '@insight/storybook';
import AuthApi from 'api/auth';
import { DataResponse } from '@insight/types';

import Login from './LoginPage';

export default {
  title: 'Auth/pages/LoginPage',
};

export const Base = () => {
  return <Login />;
};
Base.story = configureStory({
  setupMocks: (sandbox) => {
    return sandbox.stub(AuthApi.sso.session, 'login').callsFake(() => {
      return new Promise((resolve) =>
        setTimeout(() => resolve({ data: true }), 10)
      );
    });
  },
});

export const InvalidPassword = () => {
  return <Login />;
};
InvalidPassword.story = configureStory({
  setupMocks: (sandbox) => {
    return sandbox.stub(AuthApi.sso.session, 'login').callsFake(() => {
      const error = mockApiError({
        statusCode: 400,
        reason: 'Bad Request',
        message: 'Invalid email or password',
      });

      return new Promise((_resolve, reject) =>
        setTimeout(() => reject(error), 10)
      ) as Promise<DataResponse<boolean>>;
    });
  },
});

export const WithSsoRedirect = () => {
  return <Login />;
};
WithSsoRedirect.story = configureStory({
  setupMocks: (sandbox) => {
    return sandbox.stub(AuthApi.sso.session, 'login').rejects(
      mockApiError({
        statusCode: 400,
        reason: 'Bad Request',
        message: 'SSO login required',
        errors: {
          goto:
            'https://snuderls.okta.com/app/snuderlsorg446661_insightdev_1/exkw843tlucjMJ0kL4x6/sso/saml?RelayState=0TA5X6mX7YjV5Uxszm8q3p2RVfhttp%3A%2F%2Flocalhost%3A3000%2Faccount%2Fsettings',
        },
      })
    );
  },
});
