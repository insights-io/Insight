import React from 'react';
import { fullHeightDecorator } from '@insight/storybook';
import type { Meta } from '@storybook/react';
import { SSO_SETUP_DTO } from 'test/data';

import { OrganizationSettingsAuthPage } from './OrganizationSettingsAuthPage';

export default {
  title: 'settings/pages/organization/OrganizationSettingsAuthPage',
  component: OrganizationSettingsAuthPage,
  decorators: [fullHeightDecorator],
} as Meta;

export const Base = () => {
  return <OrganizationSettingsAuthPage maybeSsoSetup={SSO_SETUP_DTO} />;
};

export const WithNoSetup = () => {
  return <OrganizationSettingsAuthPage maybeSsoSetup={undefined} />;
};
