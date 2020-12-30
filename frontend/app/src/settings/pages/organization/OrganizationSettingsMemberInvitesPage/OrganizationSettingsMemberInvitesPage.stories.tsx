import React from 'react';
import { configureStory, fullHeightDecorator } from '@rebrowse/storybook';
import { REBROWSE_ADMIN_DTO } from '__tests__/data/user';
import {
  ADMIN_TEAM_INVITE_DTO,
  EXPIRED_TEAM_INVITE_DTO,
  REBROWSE_ORGANIZATION_DTO,
  STANDARD_TEAM_INVITE_DTO,
} from '__tests__/data/organization';
import type { Meta } from '@storybook/react';
import { mockOrganizationSettingsMemberInvitesPage } from '__tests__/mocks';

import { OrganizationSettingsMemberInvitesPage } from './OrganizationSettingsMemberInvitesPage';

export default {
  title: 'settings/pages/organization/OrganizationSettingsMemberInvitesPage',
  component: OrganizationSettingsMemberInvitesPage,
  decorators: [fullHeightDecorator],
} as Meta;

const invites = [
  ADMIN_TEAM_INVITE_DTO,
  EXPIRED_TEAM_INVITE_DTO,
  STANDARD_TEAM_INVITE_DTO,
];

export const Base = () => {
  return (
    <OrganizationSettingsMemberInvitesPage
      organization={REBROWSE_ORGANIZATION_DTO}
      user={REBROWSE_ADMIN_DTO}
      invites={invites}
      inviteCount={invites.length}
    />
  );
};
Base.story = configureStory({
  setupMocks: (sandbox) =>
    mockOrganizationSettingsMemberInvitesPage(sandbox, { invites }),
});
