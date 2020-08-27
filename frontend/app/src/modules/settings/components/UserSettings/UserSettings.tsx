import React from 'react';
import { Block } from 'baseui/block';
import { Table } from 'baseui/table';
import { User } from '@insight/types';
import { useStyletron } from 'baseui';
import FlexColumn from 'shared/components/FlexColumn';

import ChangePassword from '../ChangePassword';
import Security from '../Security';

type Props = {
  user: User;
  loading: boolean;
};

const UserSettings = ({ user, loading }: Props) => {
  const [_css, theme] = useStyletron();

  return (
    <Block display="flex">
      <FlexColumn flex="1">
        <Block display="flex" flex="1">
          <Block width="100%" height="fit-content">
            <Table
              isLoading={loading}
              columns={['User Information']}
              data={[
                ['Full name', user?.fullName],
                ['Email', user?.email],
                ['Organization ID', user?.organizationId],
                ['Member since', user?.createdAt.toLocaleDateString()],
              ]}
            />
          </Block>
        </Block>
        <Block>
          <Security user={user} />
        </Block>
      </FlexColumn>

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
  );
};

export default UserSettings;
