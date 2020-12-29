import React from 'react';
import { configureStory, fullHeightDecorator } from '@rebrowse/storybook';
import type { Meta } from '@storybook/react';
import {
  REBROWSE_ADMIN_DTO,
  REBROWSE_ORGANIZATION_DTO,
  SSO_SAML_SETUP_DTO,
} from '__tests__/data';
import { AuthApi } from 'api';
import {
  SamlConfigurationDTO,
  SamlSsoMethod,
  SsoSetupDTO,
} from '@rebrowse/types';

import { OrganizationSettingsAuthPage } from './OrganizationSettingsAuthPage';

export default {
  title: 'settings/pages/organization/OrganizationSettingsAuthPage',
  component: OrganizationSettingsAuthPage,
  decorators: [fullHeightDecorator],
} as Meta;

const baseProps = {
  user: REBROWSE_ADMIN_DTO,
  organization: REBROWSE_ORGANIZATION_DTO,
};

export const WithSaml = () => {
  return (
    <OrganizationSettingsAuthPage
      {...baseProps}
      maybeSsoSetup={SSO_SAML_SETUP_DTO as SsoSetupDTO}
    />
  );
};
WithSaml.story = configureStory({
  setupMocks: (sandbox) => {
    return sandbox
      .stub(AuthApi.sso.setup, 'create')
      .callsFake((method, saml) => {
        return Promise.resolve({
          data: {
            data: {
              ...SSO_SAML_SETUP_DTO,
              method: method as SamlSsoMethod,
              saml: saml as SamlConfigurationDTO,
            },
          },
          statusCode: 200,
          headers: new Headers(),
        });
      });
  },
});

export const WithNoSetup = () => {
  return (
    <OrganizationSettingsAuthPage {...baseProps} maybeSsoSetup={undefined} />
  );
};
