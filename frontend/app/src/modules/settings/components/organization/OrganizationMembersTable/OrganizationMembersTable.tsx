import React from 'react';
import { Block } from 'baseui/block';
import { Table } from 'baseui/table';
import type { User } from '@insight/types';

type Props = {
  members: User[];
};

export const OrganizationMembersTable = ({ members }: Props) => {
  return (
    <Block width="100%" height="fit-content">
      <Table
        columns={['Email', 'Full name', 'Role', 'Member since']}
        data={members.map((user) => [
          user.email,
          user.fullName,
          user.role,
          new Date(user.createdAt).toLocaleDateString(),
        ])}
      />
    </Block>
  );
};
