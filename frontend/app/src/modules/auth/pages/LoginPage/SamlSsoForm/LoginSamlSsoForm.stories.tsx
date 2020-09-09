import { configureStory } from '@insight/storybook';
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
    return sandbox
      .stub(AuthApi.sso.setup, 'getByDomain')
      .resolves({ data: true });
  },
});

export const SsoNotEnabled = () => {
  return <LoginSamlSsoForm encodedRedirect={encodeURIComponent('/')} />;
};
SsoNotEnabled.story = configureStory({
  setupMocks: (sandbox) => {
    return sandbox
      .stub(AuthApi.sso.setup, 'getByDomain')
      .resolves({ data: false });
  },
});
