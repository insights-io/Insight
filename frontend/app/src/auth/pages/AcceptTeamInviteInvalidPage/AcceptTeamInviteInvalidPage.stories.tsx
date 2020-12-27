import React from 'react';
import type { Meta } from '@storybook/react';
import { EXPIRED_TEAM_INVITE_DTO } from '__tests__/data/organization';

import { AcceptTeamInviteInvalidPage } from './AcceptTeamInviteInvalidPage';

export default {
  title: 'Auth/pages/AcceptTeamInviteInvalidPage',
  component: AcceptTeamInviteInvalidPage,
} as Meta;

export const NotFound = () => {
  return <AcceptTeamInviteInvalidPage />;
};

export const Expired = () => {
  return (
    <AcceptTeamInviteInvalidPage
      expiresAt={EXPIRED_TEAM_INVITE_DTO.expiresAt}
    />
  );
};
