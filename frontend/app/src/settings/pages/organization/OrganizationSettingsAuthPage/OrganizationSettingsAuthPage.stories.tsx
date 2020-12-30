import React from 'react';
import { configureStory, fullHeightDecorator } from '@rebrowse/storybook';
import type { Meta } from '@storybook/react';
import {
  REBROWSE_ADMIN_DTO,
  REBROWSE_ORGANIZATION_DTO,
  SSO_SAML_SETUP_DTO,
} from '__tests__/data';
import { mockOrganizationAuthPage as setupMocks } from '__tests__/mocks';

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
      maybeSsoSetup={SSO_SAML_SETUP_DTO}
    />
  );
};
WithSaml.story = configureStory({
  setupMocks: (sandbox) =>
    setupMocks(sandbox, { ssoSetup: SSO_SAML_SETUP_DTO }),
});

export const WithNoSetup = () => {
  return (
    <OrganizationSettingsAuthPage {...baseProps} maybeSsoSetup={undefined} />
  );
};
WithNoSetup.story = configureStory({
  setupMocks,
});
