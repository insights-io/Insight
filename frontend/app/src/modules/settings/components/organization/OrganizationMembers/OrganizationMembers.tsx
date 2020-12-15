import React, { useCallback, useMemo } from 'react';
import { Block } from 'baseui/block';
import {
  Button,
  Flex,
  SpacedBetween,
  VerticalAligned,
  Table,
} from '@rebrowse/elements';
import { Avatar } from 'baseui/avatar';
import { SIZE } from 'baseui/button';
import { Delete } from 'baseui/icon';
import { PLACEMENT, StatefulTooltip } from 'baseui/tooltip';
import { mapUser } from '@rebrowse/sdk';
import { AuthApi } from 'api';
import { useStyletron } from 'baseui';
import { useResourceSearch } from 'shared/hooks/useResourceSearch';
import { StyledSpinnerNext } from 'baseui/spinner';
import type {
  OrganizationDTO,
  SearchBean,
  User,
  UserDTO,
} from '@rebrowse/types';
import { capitalize } from 'shared/utils/string';

type Props = {
  members: UserDTO[];
  memberCount: number;
  user: User;
};

const NUM_ITEMS_PER_PAGE = 20;

export const OrganizationMembers = ({
  user: _user,
  members: initialMembers,
  memberCount: initialMemberCount,
}: Props) => {
  const [_css, theme] = useStyletron();

  const search = useCallback(async (search: SearchBean<UserDTO>) => {
    return AuthApi.organization.members({ search });
  }, []);

  const searchCount = useCallback(async (search: SearchBean<UserDTO>) => {
    return AuthApi.organization.memberCount({ search });
  }, []);

  const {
    page,
    onPageChange,
    query,
    setQuery,
    numPages,
    items,
    isSearching,
  } = useResourceSearch({
    resource: 'members',
    field: 'createdAt',
    initialData: { count: initialMemberCount, items: initialMembers },
    search,
    searchCount,
    numItemsPerPage: NUM_ITEMS_PER_PAGE,
  });

  const members = useMemo(() => items.map(mapUser), [items]);

  return (
    <Block>
      <Table.Header
        placeholder="Search members"
        size={SIZE.compact}
        value={query}
        onChange={(event) => setQuery(event.currentTarget.value)}
        clearable
        theme={theme}
        endEnhancer={isSearching ? <StyledSpinnerNext size={16} /> : undefined}
      />

      <Table.Body
        items={members}
        itemKey={(member) => member.id}
        header="Members"
      >
        {({ fullName, email, role }) => {
          const name = fullName || email;
          return (
            <SpacedBetween>
              <Flex width="100%" maxWidth="400px">
                <Avatar name={name} />
                <VerticalAligned marginLeft="16px">
                  <span>{name}</span>
                  <span>{email}</span>
                </VerticalAligned>
              </Flex>

              <SpacedBetween flex={1}>
                <VerticalAligned>
                  <span>{capitalize(role)}</span>
                </VerticalAligned>

                <StatefulTooltip
                  content="You cannot leave this organization as you are the only organization owner"
                  placement={PLACEMENT.top}
                  showArrow
                >
                  <VerticalAligned>
                    <Button size={SIZE.compact} disabled>
                      <Delete /> Leave
                    </Button>
                  </VerticalAligned>
                </StatefulTooltip>
              </SpacedBetween>
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
        overrides={{ Select: { props: { disabled: true } } }}
      />
    </Block>
  );
};
