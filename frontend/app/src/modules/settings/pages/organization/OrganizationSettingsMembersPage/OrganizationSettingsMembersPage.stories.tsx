import React from 'react';
import { fullHeightDecorator } from '@rebrowse/storybook';
import { INSIGHT_ADMIN_DTO, NAMELESS_INSIGHT_ADMIN_DTO } from 'test/data';
import { INSIGHT_ORGANIZATION_DTO } from 'test/data/organization';
import type { Meta } from '@storybook/react';

import { OrganizationSettingsMembersPage } from './OrganizationSettingsMembersPage';

export default {
  title: 'settings/pages/organization/OrganizationSettingsMembersPage',
  component: OrganizationSettingsMembersPage,
  decorators: [fullHeightDecorator],
} as Meta;

export const Base = () => {
  return (
    <OrganizationSettingsMembersPage
      organization={INSIGHT_ORGANIZATION_DTO}
      user={INSIGHT_ADMIN_DTO}
      members={[INSIGHT_ADMIN_DTO, NAMELESS_INSIGHT_ADMIN_DTO]}
      memberCount={2}
    />
  );
};
