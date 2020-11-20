import React from 'react';
import { fullHeightDecorator } from '@rebrowse/storybook';
import type { Meta } from '@storybook/react';
import { INSIGHT_ADMIN_DTO } from 'test/data';
import { INSIGHT_ORGANIZATION_DTO } from 'test/data/organization';

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
      user={INSIGHT_ADMIN_DTO}
      organization={INSIGHT_ORGANIZATION_DTO}
    />
  );
};
