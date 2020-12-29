import { configureStory } from '@rebrowse/storybook';
import { AuthApi } from 'api';
import React from 'react';
import { ACCOUNT_SETTINGS_PAGE } from 'shared/constants/routes';
import type { Meta } from '@storybook/react';

import { LoginSamlSsoForm } from './LoginSamlSsoForm';

export default {
  title: 'Auth/components/LoginSamlSsoForm',
  component: LoginSamlSsoForm,
} as Meta;

export const Base = () => {
  return <LoginSamlSsoForm absoluteRedirect="http://localhost:3000" />;
};
Base.story = configureStory({
  setupMocks: (sandbox) => {
    return sandbox.stub(AuthApi.sso.setup, 'getByDomain').resolves({
      data: { data: 'http://localhost:8080/v1/sso/saml/signin' },
      statusCode: 200,
      headers: new Headers(),
    });
  },
});

export const SsoNotEnabled = () => {
  return (
    <LoginSamlSsoForm
      absoluteRedirect={`http://localhost:3000${ACCOUNT_SETTINGS_PAGE}`}
    />
  );
};
SsoNotEnabled.story = configureStory({
  setupMocks: (sandbox) => {
    return sandbox.stub(AuthApi.sso.setup, 'getByDomain').resolves({
      data: { data: false },
      statusCode: 200,
      headers: new Headers(),
    });
  },
});
