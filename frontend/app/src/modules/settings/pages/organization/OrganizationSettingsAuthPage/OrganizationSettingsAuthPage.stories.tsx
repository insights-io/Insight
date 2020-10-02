import React from 'react';
import { fullHeightDecorator } from '@insight/storybook';
import type { Meta } from '@storybook/react';
import { INSIGHT_ADMIN_DTO, SSO_SETUP_DTO } from 'test/data';
import { INSIGHT_ORGANIZATION_DTO } from 'test/data/organization';

import { OrganizationSettingsAuthPage } from './OrganizationSettingsAuthPage';

export default {
  title: 'settings/pages/organization/OrganizationSettingsAuthPage',
  component: OrganizationSettingsAuthPage,
  decorators: [fullHeightDecorator],
} as Meta;

export const Base = () => {
  return (
    <OrganizationSettingsAuthPage
      maybeSsoSetup={SSO_SETUP_DTO}
      user={INSIGHT_ADMIN_DTO}
      organization={INSIGHT_ORGANIZATION_DTO}
    />
  );
};

export const WithNoSetup = () => {
  return (
    <OrganizationSettingsAuthPage
      maybeSsoSetup={undefined}
      user={INSIGHT_ADMIN_DTO}
      organization={INSIGHT_ORGANIZATION_DTO}
    />
  );
};
