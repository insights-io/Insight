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
  user: initialUser,
  organization: intialOrganization,
  members,
  memberCount,
}: Props) => {
  return (
    <OrganizationMembersLayout
      pageTab="Members"
      organization={intialOrganization}
      user={initialUser}
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
