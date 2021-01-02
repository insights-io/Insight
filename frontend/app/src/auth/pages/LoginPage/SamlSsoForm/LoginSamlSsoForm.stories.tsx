import { configureStory } from '@rebrowse/storybook';
import React from 'react';
import { ACCOUNT_SETTINGS_PAGE } from 'shared/constants/routes';
import type { Meta } from '@storybook/react';
import { httpOkResponse } from '__tests__/utils/request';
import { client } from 'sdk';

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
    return sandbox
      .stub(client.auth.sso.setups, 'retrieveByDomain')
      .resolves(httpOkResponse('http://localhost:8080/v1/sso/saml/signin'));
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
    return sandbox
      .stub(client.auth.sso.setups, 'retrieveByDomain')
      .resolves(httpOkResponse(false));
  },
});
