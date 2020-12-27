import React from 'react';
import { fullHeightDecorator } from '@rebrowse/storybook';
import { REBROWSE_ADMIN_DTO } from '__tests__/data/user';
import {
  ADMIN_TEAM_INVITE_DTO,
  EXPIRED_TEAM_INVITE_DTO,
  REBROWSE_ORGANIZATION_DTO,
  STANDARD_TEAM_INVITE_DTO,
} from '__tests__/data/organization';
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
      organization={REBROWSE_ORGANIZATION_DTO}
      user={REBROWSE_ADMIN_DTO}
      invites={[
        ADMIN_TEAM_INVITE_DTO,
        EXPIRED_TEAM_INVITE_DTO,
        STANDARD_TEAM_INVITE_DTO,
      ]}
      inviteCount={3}
    />
  );
};
