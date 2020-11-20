import React from 'react';
import { fullHeightDecorator } from '@rebrowse/storybook';
import type { Meta } from '@storybook/react';
import { REBROWSE_ORGANIZATION_DTO, REBROWSE_ADMIN_DTO } from 'test/data';

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
