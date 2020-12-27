import React from 'react';
import { Block } from 'baseui/block';
import { Table } from 'baseui/table';
import type { Organization } from '@rebrowse/types';

type Props = {
  organization: Organization;
};

export const OrganizationInfoTable = ({ organization }: Props) => {
  return (
    <Block width="100%" height="fit-content">
      <Table
        columns={['Organization information']}
        data={[
          ['ID', organization.id],
          ['Name', organization.name],
          ['Created at', organization.createdAt.toLocaleDateString()],
          ['Updated at', organization.updatedAt.toLocaleDateString()],
        ]}
      />
    </Block>
  );
};
