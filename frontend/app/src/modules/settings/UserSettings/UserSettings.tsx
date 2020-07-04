import React from 'react';
import { Block } from 'baseui/block';
import { Table } from 'baseui/table';
import { User } from '@insight/types';
import { useStyletron } from 'baseui';

import ChangePassword from '../ChangePassword';

type Props = {
  user: User | undefined;
  loading: boolean;
};

const UserSettings = ({ user, loading }: Props) => {
  const [_css, theme] = useStyletron();

  return (
    <Block display="flex">
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
