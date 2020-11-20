import React from 'react';
import { fullHeightDecorator } from '@rebrowse/storybook';
import { INSIGHT_ADMIN_DTO } from 'test/data';
import {
  ADMIN_TEAM_INVITE_DTO,
  EXPIRED_TEAM_INVITE_DTO,
  INSIGHT_ORGANIZATION_DTO,
  STANDARD_TEAM_INVITE_DTO,
} from 'test/data/organization';
import type { Meta } from '@storybook/react';

import { OrganizationSettingsMemberInvitesPage } from './OrganizationSettingsMemberInvitesPage';

export default {
  title: 'settings/pages/organization/OrganizationSettingsMemberInvitesPage',
  component: OrganizationSettingsMemberInvitesPage,
  decorators: [fullHeightDecorator],
} as Meta;

export const Base = () => {
  return (
    <OrganizationSettingsMemberInvitesPage
      organization={INSIGHT_ORGANIZATION_DTO}
      user={INSIGHT_ADMIN_DTO}
      invites={[
        ADMIN_TEAM_INVITE_DTO,
        EXPIRED_TEAM_INVITE_DTO,
        STANDARD_TEAM_INVITE_DTO,
      ]}
      inviteCount={3}
    />
  );
};
