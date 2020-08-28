import React from 'react';
import AppLayout from 'modules/app/components/AppLayout';
import { H1 } from 'baseui/typography';
import { useStyletron } from 'baseui';
import { Tabs, Tab } from 'baseui/tabs';
import { User } from '@insight/types';
import useAuth from 'modules/auth/hooks/useAuth';

import UserSettings from '../UserSettings';
import OrganizationSettings from '../OrganizationSettings';

type Props = {
  activeKey: string;
  onTabChange: (key: string) => void;
  user: User;
};

const AccountSettings = ({
  activeKey,
  onTabChange,
  user: initialUser,
}: Props) => {
  const { user, updateUser, updateUserCache } = useAuth(initialUser);
  const [_css, theme] = useStyletron();

  return (
    <AppLayout
      overrides={{
        MainContent: {
          className: 'account-settings',
          style: { padding: theme.sizing.scale400 },
        },
      }}
    >
      <H1
        margin={0}
        $style={{
          fontSize: '26px',
          lineHeight: '26px',
          color: theme.colors.mono900,
        }}
      >
        Account settings
      </H1>

      <Tabs
        renderAll={false}
        activeKey={activeKey}
        onChange={(args) => onTabChange(String(args.activeKey))}
        overrides={{
          Root: { style: { marginTop: theme.sizing.scale500 } },
          TabBar: {
            style: {
              backgroundColor: 'inherit',
              paddingLeft: 0,
              paddingRight: 0,
            },
          },
          TabContent: {
            style: { paddingLeft: 0, paddingRight: 0 },
          },
        }}
      >
        <Tab key="/account/settings" title="User settings">
          <UserSettings
            user={user}
            updateUser={updateUser}
            updateUserCache={updateUserCache}
          />
        </Tab>
        <Tab key="/account/organization-settings" title="Organization settings">
          <OrganizationSettings />
        </Tab>
        <Tab key="/account/api-keys" title="API Keys">
          <div>TODO</div>
        </Tab>
      </Tabs>
    </AppLayout>
  );
};

export default AccountSettings;
