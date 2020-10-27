import React, { useState } from 'react';
import {
  SETTINGS_PATH_PART,
  ORGANIZATION_SETTINGS_PAGE_PART,
  ORGANIZATION_SETTINGS_MEMBERS_PAGE_PART,
} from 'shared/constants/routes';
import { OrganizationSettingsPageLayout } from 'modules/settings/components/organization/OrganizationSettingsPageLayout';
import { OrganizationMembers } from 'modules/settings/components/organization/OrganizationMembers';
import { TeamInvites } from 'modules/settings/components/organization/TeamInvites';
import type { Path } from 'modules/settings/types';
import type { OrganizationDTO, TeamInviteDTO, UserDTO } from '@insight/types';
import { useUser } from 'shared/hooks/useUser';
import { useOrganization } from 'shared/hooks/useOrganization';
import { Tabs, Tab } from 'baseui/tabs-motion';

const PATH: Path = [
  SETTINGS_PATH_PART,
  ORGANIZATION_SETTINGS_PAGE_PART,
  ORGANIZATION_SETTINGS_MEMBERS_PAGE_PART,
];

type PageTab = 'Invites' | 'Members';

type Props = {
  members: UserDTO[];
  memberCount: number;
  teamInvites: TeamInviteDTO[];
  inviteCount: number;
  user: UserDTO;
  organization: OrganizationDTO;
  pageTab: PageTab;
};

export const OrganizationSettingsMembersPage = ({
  members: initialMembers,
  memberCount: initialMemberCount,
  teamInvites: initialTeamInvites,
  inviteCount: initialInviteCount,
  user: initialUser,
  organization: initialOrganization,
  pageTab,
}: Props) => {
  const { organization } = useOrganization(initialOrganization);
  const { user } = useUser(initialUser);
  const [activeKey, setActiveKey] = useState(pageTab);

  return (
    <OrganizationSettingsPageLayout
      header="Members"
      path={PATH}
      user={user}
      organization={organization}
    >
      <Tabs
        activeKey={activeKey}
        onChange={(params) => setActiveKey(params.activeKey as PageTab)}
        activateOnFocus
      >
        <Tab title="Members" key="Members">
          <OrganizationMembers
            members={initialMembers}
            memberCount={initialMemberCount}
            user={user}
          />
        </Tab>
        <Tab title="Team invites" key="Team invites">
          <TeamInvites
            invites={initialTeamInvites}
            inviteCount={initialInviteCount}
          />
        </Tab>
      </Tabs>
    </OrganizationSettingsPageLayout>
  );
};
