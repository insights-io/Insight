import { configureStory, mockApiError } from '@insight/storybook';
import { AuthApi } from 'api';
import React from 'react';

import LoginSamlSsoForm from './LoginSamlSsoForm';

export default {
  title: 'Auth/components/LoginSamlSsoForm',
};

export const Base = () => {
  return <LoginSamlSsoForm encodedRedirect={encodeURIComponent('/')} />;
};
Base.story = configureStory({
  setupMocks: (sandbox) => {
    return sandbox.stub(AuthApi.ssoSetup, 'get').resolves({
      data: {
        configurationEndpoint: '',
        createdAt: new Date().toISOString(),
        domain: 'snuderls.eu',
        organizationId: '000001',
        type: 'OKTA',
      },
    });
  },
});

export const SsoNotEnabled = () => {
  return <LoginSamlSsoForm encodedRedirect={encodeURIComponent('/')} />;
};
SsoNotEnabled.story = configureStory({
  setupMocks: (sandbox) => {
    return sandbox.stub(AuthApi.ssoSetup, 'get').rejects(
      mockApiError({
        message: 'That email or domain isn’t registered for SSO',
        reason: 'Not Found',
        statusCode: 404,
      })
    );
  },
});
