import React, { useMemo, useState } from 'react';
import { Block } from 'baseui/block';
import type {
  TeamInvite,
  TeamInviteCreateDTO,
  TeamInviteDTO,
} from '@insight/types';
import {
  Button,
  Flex,
  SpacedBetween,
  VerticalAligned,
  Table,
} from '@insight/elements';
import { Avatar } from 'baseui/avatar';
import { capitalize } from 'modules/billing/utils';
import { SIZE } from 'baseui/button';
import { Delete, Plus } from 'baseui/icon';
import { useStyletron } from 'baseui';

import TeamInviteModal from '../TeamInviteModal';

type Props = {
  invites: TeamInvite[];
  createTeamInvite: (data: TeamInviteCreateDTO) => Promise<TeamInviteDTO>;
};

export const TeamInvites = ({ invites, createTeamInvite }: Props) => {
  const [page, setPage] = useState(1);
  const [query, setQuery] = useState('');
  const [_css, theme] = useStyletron();
  const numPages = useMemo(() => Math.ceil(invites.length / 20), [invites]);

  return (
    <Block width="100%">
      <Table.Header
        theme={theme}
        placeholder="Search invites"
        value={query}
        onChange={(event) => setQuery(event.currentTarget.value)}
        clearable
      >
        <TeamInviteModal createTeamInvite={createTeamInvite}>
          {(open) => (
            <Block marginLeft="16px" width="240px">
              <Button onClick={open} $style={{ width: '100%' }}>
                <Plus /> Invite member
              </Button>
            </Block>
          )}
        </TeamInviteModal>
      </Table.Header>

      <Table.Body
        header="Team invites"
        items={invites}
        itemKey={(invite) => invite.token}
      >
        {({ token, email: name, role }) => {
          return (
            <SpacedBetween key={token}>
              <Flex>
                <Avatar name={name} />
                <VerticalAligned marginLeft="16px">
                  <span>{name}</span>
                </VerticalAligned>
              </Flex>
              <VerticalAligned>
                <span>{capitalize(role)}</span>
              </VerticalAligned>
              <VerticalAligned>
                <Button size={SIZE.compact} disabled>
                  <Delete /> Revoke
                </Button>
              </VerticalAligned>
            </SpacedBetween>
          );
        }}
      </Table.Body>

      <Table.Footer
        numPages={numPages}
        currentPage={page}
        size={SIZE.compact}
        onPageChange={({ nextPage }) => {
          setPage(Math.min(Math.max(nextPage, 1), numPages));
        }}
        theme={theme}
      />
    </Block>
  );
};
