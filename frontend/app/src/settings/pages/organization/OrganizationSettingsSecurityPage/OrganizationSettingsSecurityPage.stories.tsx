import React from 'react';
import { configureStory, fullHeightDecorator } from '@rebrowse/storybook';
import type { Meta } from '@storybook/react';
import { REBROWSE_ORGANIZATION_DTO, REBROWSE_ADMIN_DTO } from '__tests__/data';
import { mockOrganizationSettingsSecurityPage as setupMocks } from '__tests__/mocks';

import { OrganizationSettingsSecurityPage } from './OrganizationSettingsSecurityPage';

export default {
  title: 'settings/pages/organization/OrganizationSettingsSecurityPage',
  component: OrganizationSettingsSecurityPage,
  decorators: [fullHeightDecorator],
} as Meta;

export const Base = () => {
  return (
    <OrganizationSettingsSecurityPage
      passwordPolicy={undefined}
      user={REBROWSE_ADMIN_DTO}
      organization={REBROWSE_ORGANIZATION_DTO}
    />
  );
};
Base.story = configureStory({ setupMocks });
