import React from 'react';
import { useStyletron } from 'baseui';
import { Block } from 'baseui/block';
import TeamInviteModal from 'modules/settings/components/organization/TeamInviteModal';
import { H3 } from 'baseui/typography';
import { Table } from 'baseui/table';
import { StatefulTooltip } from 'baseui/tooltip';
import VerticalAligned from 'shared/components/VerticalAligned';
import { Alert } from 'baseui/icon';
import { differenceInSeconds } from 'date-fns';
import type {
  TeamInvite,
  TeamInviteCreateDTO,
  TeamInviteDTO,
} from '@insight/types';

type Props = {
  invites: TeamInvite[];
  createTeamInvite: (data: TeamInviteCreateDTO) => Promise<TeamInviteDTO>;
};

const SECONDS_IN_DAY = 86400;

export const TeamInvites = ({ invites, createTeamInvite }: Props) => {
  const [_css, theme] = useStyletron();

  const dataColumn = (invite: TeamInvite) => {
    const isTeamInviteValid =
      differenceInSeconds(new Date(), invite.createdAt) < SECONDS_IN_DAY;

    return [
      invite.email,
      invite.role,
      invite.createdAt.toLocaleDateString(),
      <>
        {String(isTeamInviteValid)}
        {!isTeamInviteValid && (
          <StatefulTooltip
            showArrow
            placement="top"
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
    <Block
      overrides={{ Block: { props: { className: 'invites' } } }}
      marginTop={theme.sizing.scale800}
    >
      <Block display="flex" justifyContent="space-between" marginBottom="12px">
        <H3 margin="0" $style={{ fontSize: '18px', lineHeight: '18px' }}>
          Team invites
        </H3>
        <TeamInviteModal createTeamInvite={createTeamInvite} />
      </Block>
      <Block width="100%" height="fit-content">
        <Table
          columns={['Email', 'Role', 'Invited on', 'Valid']}
          data={invites.map(dataColumn)}
        />
      </Block>
    </Block>
  );
};
