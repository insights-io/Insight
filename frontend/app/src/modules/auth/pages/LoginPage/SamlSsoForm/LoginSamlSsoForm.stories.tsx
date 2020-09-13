import { configureStory } from '@insight/storybook';
import { AuthApi } from 'api';
import React from 'react';

import LoginSamlSsoForm from './LoginSamlSsoForm';

export default {
  title: 'Auth/components/LoginSamlSsoForm',
};

export const Base = () => {
  return <LoginSamlSsoForm absoluteRedirect="http://localhost:3000" />;
};
Base.story = configureStory({
  setupMocks: (sandbox) => {
    return sandbox
      .stub(AuthApi.sso.setup, 'getByDomain')
      .resolves({ data: 'http://localhost:8080/v1/sso/saml/signin' });
  },
});

export const SsoNotEnabled = () => {
  return (
    <LoginSamlSsoForm absoluteRedirect="http://localhost:3000/account/settings" />
  );
};
SsoNotEnabled.story = configureStory({
  setupMocks: (sandbox) => {
    return sandbox
      .stub(AuthApi.sso.setup, 'getByDomain')
      .resolves({ data: false });
  },
});
