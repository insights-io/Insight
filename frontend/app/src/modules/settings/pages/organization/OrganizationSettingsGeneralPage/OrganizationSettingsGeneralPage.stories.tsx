import React from 'react';
import { fullHeightDecorator } from '@rebrowse/storybook';
import { INSIGHT_ORGANIZATION_DTO } from 'test/data/organization';
import type { Meta } from '@storybook/react';
import { INSIGHT_ADMIN_DTO } from 'test/data';

import { OrganizationSettingsGeneralPage } from './OrganizationSettingsGeneralPage';

export default {
  title: 'settings/pages/organization/OrganizationSettingsGeneralPage',
  component: OrganizationSettingsGeneralPage,
  decorators: [fullHeightDecorator],
} as Meta;

export const Base = () => {
  return (
    <OrganizationSettingsGeneralPage
      organization={INSIGHT_ORGANIZATION_DTO}
      user={INSIGHT_ADMIN_DTO}
    />
  );
};
