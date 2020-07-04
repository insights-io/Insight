import React from 'react';
import { Table } from 'baseui/table';
import { Block } from 'baseui/block';
import useSWR from 'swr';
import OrganizationsApi from 'api/organizations';
import { H3 } from 'baseui/typography';
import { useStyletron } from 'baseui';

const OrganizationSettings = () => {
  const [_css, theme] = useStyletron();
  const { data: organizationMembers } = useSWR('OrganizationsApi.members', () =>
    OrganizationsApi.members()
  );

  const { data: organization } = useSWR('OrganizationsApi.get', () =>
    OrganizationsApi.get()
  );

  return (
    <Block>
      <Block marginBottom={theme.sizing.scale800}>
        <H3
          margin="0"
          marginBottom={theme.sizing.scale400}
          $style={{ fontSize: '18px', lineHeight: '18px' }}
        >
          Details
        </H3>
        <Block width="100%" height="fit-content">
          <Table
            isLoading={organization === undefined}
            columns={['Organization information']}
            data={[
              ['ID', organization?.id],
              ['Name', organization?.name],
              ['Created at', organization?.createdAt.toLocaleDateString()],
            ]}
          />
        </Block>
      </Block>
      <Block overrides={{ Block: { props: { className: 'members' } } }}>
        <H3
          margin="0"
          marginBottom={theme.sizing.scale400}
          $style={{ fontSize: '18px', lineHeight: '18px' }}
        >
          Members
        </H3>
        <Block width="100%" height="fit-content">
          <Table
            isLoading={organizationMembers === undefined}
            columns={['Email', 'Full name', 'Role', 'Member since']}
            data={(organizationMembers || []).map((user) => [
              user.email,
              user.fullName,
              user.role,
              new Date(user.createdAt).toLocaleDateString(),
            ])}
          />
        </Block>
      </Block>
    </Block>
  );
};

export default OrganizationSettings;
