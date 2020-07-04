import React from 'react';
import AppLayout from 'modules/app/components/AppLayout';
import { H1 } from 'baseui/typography';
import { useStyletron } from 'baseui';
import { Tabs, Tab } from 'baseui/tabs';
import { UserDTO } from '@insight/types';
import { Table } from 'baseui/table';
import { Block } from 'baseui/block';
import useAuth from 'modules/auth/hooks/useAuth';

import ChangePassword from '../ChangePassword';

type Props = {
  activeKey: string;
  onTabChange: (key: string) => void;
  initialUser?: UserDTO;
};

const AccountSettings = ({ activeKey, onTabChange, initialUser }: Props) => {
  const { user, loading: loadingUser } = useAuth(initialUser);
  const [_css, theme] = useStyletron();

  return (
    <AppLayout
      overrides={{ MainContent: { style: { padding: theme.sizing.scale400 } } }}
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
          <Block display="flex">
            <Block display="flex" flex="1">
              <Block width="100%" height="fit-content">
                <Table
                  isLoading={loadingUser}
                  columns={['User Information']}
                  data={[
                    ['Full name', user?.fullName],
                    ['Email', user?.email],
                    ['Organization', user?.organizationId],
                    ['Member since', user?.createdAt.toLocaleDateString()],
                  ]}
                />
              </Block>
            </Block>
            <ChangePassword
              overrides={{
                Root: {
                  style: {
                    maxWidth: '400px',
                    width: '100%',
                    marginLeft: theme.sizing.scale600,
                  },
                },
              }}
            />
          </Block>
        </Tab>
        <Tab key="/account/organization-settings" title="Organization settings">
          <div>TODO</div>
        </Tab>
        <Tab key="/account/api-keys" title="API Keys">
          <div>TODO</div>
        </Tab>
      </Tabs>
    </AppLayout>
  );
};

export default AccountSettings;
