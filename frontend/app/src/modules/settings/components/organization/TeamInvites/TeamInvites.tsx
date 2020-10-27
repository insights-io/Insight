import React, { useCallback, useMemo } from 'react';
import { Block } from 'baseui/block';
import type {
  SearchBean,
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
import { AuthApi } from 'api';
import { useResourceSearch } from 'shared/hooks/useResourceSearch';
import { mapTeamInvite } from '@insight/sdk';
import { useStyletron } from 'baseui';
import { Spinner } from 'baseui/spinner';

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

  const search = useCallback(async (search: SearchBean) => {
    return AuthApi.organization.teamInvite.list({ search });
  }, []);

  const searchCount = useCallback(async (search: SearchBean) => {
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
        endEnhancer={isSearching ? <Spinner size={16} /> : undefined}
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
        onPageChange={({ nextPage }) => onPageChange(nextPage)}
        theme={theme}
      />
    </Block>
  );
};
