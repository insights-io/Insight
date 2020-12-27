import React from 'react';
import {
  SETTINGS_PATH_PART,
  ORGANIZATION_SETTINGS_PAGE_PART,
  ORGANIZATION_SETTINGS_MEMBERS_PAGE_PART,
  ORGANIZATION_SETTINGS_MEMBER_INVITES_PAGE,
  ORGANIZATION_SETTINGS_MEMBERS_PAGE,
} from 'shared/constants/routes';
import { OrganizationSettingsPageLayout } from 'settings/components/organization/OrganizationSettingsPageLayout';
import type { Path } from 'settings/types';
import type {
  Organization,
  OrganizationDTO,
  User,
  UserDTO,
} from '@rebrowse/types';
import { useUser } from 'shared/hooks/useUser';
import { useOrganization } from 'shared/hooks/useOrganization';
import { Tabs, Tab } from 'baseui/tabs-motion';
import Link from 'next/link';
import { UnstyledLink } from '@rebrowse/elements';
import { useTabRoute } from 'shared/hooks/useTabRoute';

const PATH: Path = [
  SETTINGS_PATH_PART,
  ORGANIZATION_SETTINGS_PAGE_PART,
  ORGANIZATION_SETTINGS_MEMBERS_PAGE_PART,
];

const TABS = [
  { path: ORGANIZATION_SETTINGS_MEMBERS_PAGE, label: 'Members' },
  { path: ORGANIZATION_SETTINGS_MEMBER_INVITES_PAGE, label: 'Team invites' },
] as const;

type PageTab = typeof TABS[number]['label'];

type Props = {
  user: UserDTO;
  organization: OrganizationDTO;
  pageTab: PageTab;
  renderTab: (user: User, organization: Organization) => React.ReactNode;
};

let previousTab: PageTab | undefined;

export const OrganizationMembersLayout = ({
  user: initialUser,
  organization: initialOrganization,
  pageTab,
  renderTab,
}: Props) => {
  const { organization } = useOrganization(initialOrganization);
  const { user } = useUser(initialUser);
  const activeTab = useTabRoute({
    previous: previousTab,
    current: pageTab,
    setPrevious: (current) => {
      previousTab = current;
    },
  });

  return (
    <OrganizationSettingsPageLayout
      header="Members"
      path={PATH}
      user={user}
      organization={organization}
      title={pageTab}
    >
      <Tabs activeKey={activeTab} activateOnFocus>
        {TABS.map(({ path, label }) => {
          return (
            <Tab
              overrides={{
                Tab: {
                  style: {
                    paddingTop: 0,
                    paddingBottom: 0,
                    paddingLeft: 0,
                    paddingRight: 0,
                  },
                },
              }}
              key={label}
              title={
                <Link href={path}>
                  <UnstyledLink height="100%" padding="16px">
                    {label}
                  </UnstyledLink>
                </Link>
              }
            >
              {renderTab(user, organization)}
            </Tab>
          );
        })}
      </Tabs>
    </OrganizationSettingsPageLayout>
  );
};
