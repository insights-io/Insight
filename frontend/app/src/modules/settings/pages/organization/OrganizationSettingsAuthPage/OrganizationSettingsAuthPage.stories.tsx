import React from 'react';
import { configureStory, fullHeightDecorator } from '@rebrowse/storybook';
import type { Meta } from '@storybook/react';
import { INSIGHT_ADMIN_DTO, SSO_SAML_SETUP_DTO } from 'test/data';
import { INSIGHT_ORGANIZATION_DTO } from 'test/data/organization';
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
  user: INSIGHT_ADMIN_DTO,
  organization: INSIGHT_ORGANIZATION_DTO,
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
          ...SSO_SAML_SETUP_DTO,
          method: method as SamlSsoMethod,
          saml: saml as SamlConfigurationDTO,
        });
      });
  },
});

export const WithNoSetup = () => {
  return (
    <OrganizationSettingsAuthPage {...baseProps} maybeSsoSetup={undefined} />
  );
};
