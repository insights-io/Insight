import React from 'react';
import type { OrganizationDTO, UserDTO } from '@rebrowse/types';
import { OrganizationMembersLayout } from 'settings/components/organization/OrganizationMembersLayout';
import { OrganizationMembers } from 'settings/components/organization/OrganizationMembers';

type Props = {
  user: UserDTO;
  organization: OrganizationDTO;
  members: UserDTO[];
  memberCount: number;
};

export const OrganizationSettingsMembersPage = ({
  user,
  organization,
  members,
  memberCount,
}: Props) => {
  return (
    <OrganizationMembersLayout
      pageTab="Members"
      organization={organization}
      user={user}
      renderTab={(user) => (
        <OrganizationMembers
          user={user}
          members={members}
          memberCount={memberCount}
        />
      )}
    />
  );
};
