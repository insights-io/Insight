import React from 'react';
import type { Meta } from '@storybook/react';
import { ADMIN_TEAM_INVITE_DTO } from '__tests__/data';
import { configureStory } from '@rebrowse/storybook';
import { mockAcceptTeamInvitePage as setupMocks } from '__tests__/mocks';

import { AcceptTeamInvitePage } from './AcceptTeamInvitePage';

export default {
  title: 'Auth/pages/AcceptTeamInvitePage',
  component: AcceptTeamInvitePage,
} as Meta;

export const Base = () => {
  return <AcceptTeamInvitePage {...ADMIN_TEAM_INVITE_DTO} />;
};
Base.story = configureStory({ setupMocks });
