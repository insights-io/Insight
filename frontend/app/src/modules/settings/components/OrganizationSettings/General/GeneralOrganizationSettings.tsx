import { Block } from 'baseui/block';
import { H3 } from 'baseui/typography';
import React from 'react';
import { Table } from 'baseui/table';
import { useStyletron } from 'baseui';
import { Organization, TeamInvite } from '@insight/types';
import useTeamInvites from 'modules/team/hooks/useTeamInvites';
import useSWR from 'swr';
import { AuthApi } from 'api';
import { StatefulTooltip } from 'baseui/tooltip';
import VerticalAligned from 'shared/components/VerticalAligned';
import { Alert } from 'baseui/icon';
import { differenceInSeconds } from 'date-fns';
import TeamInviteModal from 'modules/settings/components/TeamInviteModal';

type Props = {
  organization: Organization | undefined;
  isLoading: boolean;
};

const GeneralOrganizationSettings = ({ organization, isLoading }: Props) => {
  const [_css, theme] = useStyletron();
  const { invites, loading: loadingInvites, createInvite } = useTeamInvites();
  const { data: members } = useSWR('AuthApi.organizations.members', () =>
    AuthApi.organization.members()
  );

  const dataColumn = (invite: TeamInvite) => {
    const teamInviteValid =
      differenceInSeconds(new Date(), invite.createdAt) < 86400;

    return [
      invite.email,
      invite.role,
      invite.createdAt.toLocaleDateString(),
      <>
        {String(teamInviteValid)}
        {!teamInviteValid && (
          <StatefulTooltip
            content={() => <Block>Team invite is valid only for 1 day</Block>}
          >
            <VerticalAligned $style={{ marginLeft: theme.sizing.scale400 }}>
              <Alert />
            </VerticalAligned>
          </StatefulTooltip>
        )}
      </>,
    ];
  };

  return (
    <>
      <Block>
        <H3
          marginTop="0"
          marginBottom={theme.sizing.scale400}
          $style={{ fontSize: '18px', lineHeight: '18px' }}
        >
          Details
        </H3>
        <Block width="100%" height="fit-content">
          <Table
            isLoading={isLoading}
            columns={['Organization information']}
            data={[
              ['ID', organization?.id],
              ['Name', organization?.name],
              ['Created at', organization?.createdAt.toLocaleDateString()],
            ]}
          />
        </Block>
      </Block>
      <Block
        overrides={{ Block: { props: { className: 'members' } } }}
        marginTop={theme.sizing.scale800}
      >
        <H3
          marginTop="0"
          marginBottom={theme.sizing.scale400}
          $style={{ fontSize: '18px', lineHeight: '18px' }}
        >
          Members
        </H3>
        <Block width="100%" height="fit-content">
          <Table
            isLoading={members === undefined}
            columns={['Email', 'Full name', 'Role', 'Member since']}
            data={(members || []).map((user) => [
              user.email,
              user.fullName,
              user.role,
              new Date(user.createdAt).toLocaleDateString(),
            ])}
          />
        </Block>
      </Block>
      <Block
        overrides={{ Block: { props: { className: 'invites' } } }}
        marginTop={theme.sizing.scale800}
      >
        <Block
          display="flex"
          justifyContent="space-between"
          marginBottom="12px"
        >
          <H3 margin="0" $style={{ fontSize: '18px', lineHeight: '18px' }}>
            Team invites
          </H3>
          <TeamInviteModal createInvite={createInvite} />
        </Block>
        <Block width="100%" height="fit-content">
          <Table
            isLoading={loadingInvites}
            columns={['Email', 'Role', 'Invited on', 'Valid']}
            data={invites.map(dataColumn)}
          />
        </Block>
      </Block>
    </>
  );
};

export default React.memo(GeneralOrganizationSettings);
