import React, { useEffect, useMemo, useState } from 'react';
import { Block } from 'baseui/block';
import type { User, UserDTO } from '@insight/types';
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
import { Delete } from 'baseui/icon';
import { PLACEMENT, StatefulTooltip } from 'baseui/tooltip';
import { mapUser } from '@insight/sdk';
import { AuthApi } from 'api';
import { useStyletron } from 'baseui';

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
  const [members, setMembers] = useState(() => initialMembers.map(mapUser));
  const [memberCount] = useState(initialMemberCount);
  const [page, setPage] = useState(1);
  const [query, setQuery] = useState('');

  const numPages = useMemo(() => Math.ceil(memberCount / NUM_ITEMS_PER_PAGE), [
    memberCount,
  ]);

  // TODO: extract hook with debounce to fetch count/members
  useEffect(() => {
    AuthApi.organization
      .members({ search: query ? { query } : undefined })
      .then((newMembers) => setMembers(newMembers.map(mapUser)));
  }, [query]);

  return (
    <Block>
      <Table.Header
        placeholder="Search members"
        value={query}
        onChange={(event) => setQuery(event.currentTarget.value)}
        clearable
        theme={theme}
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
              <Flex>
                <Avatar name={name} />
                <VerticalAligned marginLeft="16px">
                  <span>{name}</span>
                  <span>{email}</span>
                </VerticalAligned>
              </Flex>
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
