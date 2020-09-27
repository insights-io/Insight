import React from 'react';
import { fullHeightDecorator } from '@insight/storybook';
import { INSIGHT_ADMIN_DTO } from 'test/data';
import {
  ADMIN_TEAM_INVITE_DTO,
  EXPIRED_TEAM_INVITE_DTO,
  STANDARD_TEAM_INVITE_DTO,
} from 'test/data/organization';
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
      members={[INSIGHT_ADMIN_DTO]}
      teamInvites={[
        STANDARD_TEAM_INVITE_DTO,
        ADMIN_TEAM_INVITE_DTO,
        EXPIRED_TEAM_INVITE_DTO,
      ]}
    />
  );
};
