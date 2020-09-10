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
            'http://localhost:8080/v1/sso/saml/signin?redirect=%2Faccount%2Fsettings&email=7bd08ee1-baa8-421e-86d3-861508e28c31%40insight-io.com',
        },
      })
    );
  },
});
