import React, { useCallback, useMemo } from 'react';
import { Block } from 'baseui/block';
import type { TeamInviteCreateDTO, TeamInviteDTO } from '@rebrowse/types';
import {
  Button,
  Flex,
  SpacedBetween,
  VerticalAligned,
  Table,
} from '@rebrowse/elements';
import { Avatar } from 'baseui/avatar';
import { SIZE } from 'baseui/button';
import { Delete, Plus } from 'baseui/icon';
import { AuthApi } from 'api';
import { useResourceSearch } from 'shared/hooks/useResourceSearch';
import { mapTeamInvite, TeamInviteSearchBean } from '@rebrowse/sdk';
import { useStyletron } from 'baseui';
import { StyledSpinnerNext } from 'baseui/spinner';
import { capitalize } from 'shared/utils/string';

import TeamInviteModal from '../TeamInviteModal';

type Props = {
  invites: TeamInviteDTO[];
  inviteCount: number;
};

const NUM_ITEMS_PER_PAGE = 20;

export const TeamInvites = ({
  invites: initialInvites,
  inviteCount: initialInviteCount,
}: Props) => {
  const [_css, theme] = useStyletron();

  const search = useCallback(async (search: TeamInviteSearchBean) => {
    return AuthApi.organization.teamInvite.list({ search });
  }, []);

  const searchCount = useCallback(async (search: TeamInviteSearchBean) => {
    return AuthApi.organization.teamInvite.count({ search });
  }, []);

  const {
    page,
    onPageChange,
    query,
    setQuery,
    numPages,
    items,
    isSearching,
    revalidate,
  } = useResourceSearch({
    resource: 'invites',
    field: 'createdAt',
    initialData: { count: initialInviteCount, items: initialInvites },
    search,
    searchCount,
    numItemsPerPage: NUM_ITEMS_PER_PAGE,
  });

  const createTeamInvite = (data: TeamInviteCreateDTO) => {
    return AuthApi.organization.teamInvite.create(data).then((teamInvite) => {
      revalidate();
      return teamInvite;
    });
  };

  const invites = useMemo(() => items.map(mapTeamInvite), [items]);

  return (
    <Block width="100%">
      <Table.Header
        theme={theme}
        placeholder="Search invites"
        value={query}
        onChange={(event) => setQuery(event.currentTarget.value)}
        clearable
        endEnhancer={isSearching ? <StyledSpinnerNext size={16} /> : undefined}
        size={SIZE.compact}
      >
        <TeamInviteModal createTeamInvite={createTeamInvite}>
          {(open) => (
            <Block marginLeft="16px" width="240px">
              <Button
                onClick={open}
                $style={{ width: '100%' }}
                size={SIZE.compact}
              >
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
        onPageChange={({ nextPage }) => onPageChange(nextPage)}
        theme={theme}
      />
    </Block>
  );
};
