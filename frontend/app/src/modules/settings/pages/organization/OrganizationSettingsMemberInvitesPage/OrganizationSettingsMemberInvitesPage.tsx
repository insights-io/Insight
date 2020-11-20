import React from 'react';
import type { OrganizationDTO, TeamInviteDTO, UserDTO } from '@rebrowse/types';
import { OrganizationMembersLayout } from 'modules/settings/components/organization/OrganizationMembersLayout';
import { TeamInvites } from 'modules/settings/components/organization/TeamInvites';

type Props = {
  user: UserDTO;
  organization: OrganizationDTO;
  invites: TeamInviteDTO[];
  inviteCount: number;
};

export const OrganizationSettingsMemberInvitesPage = ({
  user,
  organization,
  invites,
  inviteCount,
}: Props) => {
  return (
    <OrganizationMembersLayout
      pageTab="Team invites"
      organization={organization}
      user={user}
      renderTab={() => (
        <TeamInvites invites={invites} inviteCount={inviteCount} />
      )}
    />
  );
};
