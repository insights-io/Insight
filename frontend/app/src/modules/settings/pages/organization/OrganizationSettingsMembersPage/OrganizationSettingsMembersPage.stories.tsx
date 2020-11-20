import React from 'react';
import { fullHeightDecorator } from '@rebrowse/storybook';
import {
  REBROWSE_ADMIN_DTO,
  REBROWSE_ORGANIZATION_DTO,
  NAMELESS_ADMIN_DTO,
} from 'test/data';
import type { Meta } from '@storybook/react';

import { OrganizationSettingsMembersPage } from './OrganizationSettingsMembersPage';

export default {
  title: 'settings/pages/organization/OrganizationSettingsMembersPage',
  component: OrganizationSettingsMembersPage,
  decorators: [fullHeightDecorator],
} as Meta;

export const Base = () => {
  const members = [REBROWSE_ADMIN_DTO, NAMELESS_ADMIN_DTO];

  return (
    <OrganizationSettingsMembersPage
      organization={REBROWSE_ORGANIZATION_DTO}
      user={REBROWSE_ADMIN_DTO}
      members={members}
      memberCount={members.length}
    />
  );
};
