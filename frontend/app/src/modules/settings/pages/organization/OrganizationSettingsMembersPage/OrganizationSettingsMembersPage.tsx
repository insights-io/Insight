import React, { useMemo } from 'react';
import {
  SETTINGS_PATH_PART,
  ORGANIZATION_SETTINGS_PAGE_PART,
  ORGANIZATION_SETTINGS_MEMBERS_PAGE_PART,
} from 'shared/constants/routes';
import { OrganizationSettingsPageLayout } from 'modules/settings/components/organization/OrganizationSettingsPageLayout';
import { OrganizationMembersTable } from 'modules/settings/components/organization/OrganizationMembersTable';
import useTeamInvites from 'modules/settings/hooks/useTeamInvites';
import { TeamInvites } from 'modules/settings/components/organization/TeamInvites';
import { mapUser } from '@insight/sdk';
import type { Path } from 'modules/settings/types';
import type { OrganizationDTO, TeamInviteDTO, UserDTO } from '@insight/types';
import { useUser } from 'shared/hooks/useUser';
import { useOrganization } from 'shared/hooks/useOrganization';

const PATH: Path = [
  SETTINGS_PATH_PART,
  ORGANIZATION_SETTINGS_PAGE_PART,
  ORGANIZATION_SETTINGS_MEMBERS_PAGE_PART,
];

type Props = {
  members: UserDTO[];
  teamInvites: TeamInviteDTO[];
  user: UserDTO;
  organization: OrganizationDTO;
};

export const OrganizationSettingsMembersPage = ({
  members: initialMembers,
  teamInvites: initialTeamInvites,
  user: initialUser,
  organization: initialOrganization,
}: Props) => {
  const { organization } = useOrganization(initialOrganization);
  const { user } = useUser(initialUser);
  const { invites, createTeamInvite } = useTeamInvites(initialTeamInvites);
  const members = useMemo(() => initialMembers.map(mapUser), [initialMembers]);

  return (
    <OrganizationSettingsPageLayout
      path={PATH}
      header="Members"
      user={user}
      organization={organization}
    >
      <OrganizationMembersTable members={members} />
      <TeamInvites invites={invites} createTeamInvite={createTeamInvite} />
    </OrganizationSettingsPageLayout>
  );
};
