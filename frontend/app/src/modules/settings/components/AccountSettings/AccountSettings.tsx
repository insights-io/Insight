import React from 'react';
import AppLayout from 'modules/app/components/AppLayout';
import { H1 } from 'baseui/typography';
import { useStyletron } from 'baseui';
import { Tabs, Tab } from 'baseui/tabs-motion';
import { User } from '@insight/types';
import useAuth from 'modules/auth/hooks/useAuth';
import {
  API_KEYS_SETTINGS_PAGE,
  ORGANIZATION_SETTINGS_PAGE,
  USER_SETTINGS_PAGE,
} from 'shared/constants/routes';

import UserSettings from '../UserSettings';
import OrganizationSettings from '../OrganizationSettings';

type Props = {
  activeTab: string;
  onTabChange: (key: string) => void;
  user: User;
};

const AccountSettings = ({
  activeTab,
  onTabChange,
  user: initialUser,
}: Props) => {
  const { user, updateUser, updateUserCache } = useAuth(initialUser);
  const [_css, theme] = useStyletron();

  const getActiveKey = (tab: string) => {
    return tab.split('/').slice(0, 3).join('/');
  };

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
        activeKey={getActiveKey(activeTab)}
        onChange={(args) => onTabChange(String(args.activeKey))}
        overrides={{ Root: { style: { marginTop: theme.sizing.scale600 } } }}
      >
        <Tab key={USER_SETTINGS_PAGE} title="User settings">
          <UserSettings
            user={user}
            updateUser={updateUser}
            updateUserCache={updateUserCache}
          />
        </Tab>
        <Tab key={ORGANIZATION_SETTINGS_PAGE} title="Organization settings">
          <OrganizationSettings
            activeTab={activeTab}
            onTabChange={onTabChange}
          />
        </Tab>
        <Tab key={API_KEYS_SETTINGS_PAGE} title="API Keys">
          <div>TODO</div>
        </Tab>
      </Tabs>
    </AppLayout>
  );
};

export default AccountSettings;
