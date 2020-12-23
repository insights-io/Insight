import React from 'react';
import { Block } from 'baseui/block';
import { Breadcrumbs } from 'shared/components/Breadcrumbs';
import Link from 'next/link';
import { Select, SIZE, TYPE, Option } from 'baseui/select';
import {
  FlexColumn,
  VerticalAligned,
  SpacedBetween,
  UnstyledLink,
  expandBorderRadius,
  Button,
  Flex,
} from '@rebrowse/elements';
import { FaLink } from 'react-icons/fa';
import { Menu, Delete } from 'baseui/icon';
import type { Path, SearchOption } from 'modules/settings/types';
import * as zIndex from 'shared/constants/zIndex';

type Props = {
  path: Path;
  searchOptions: SearchOption[];
  isSidebarOverlay: boolean;
  overlaySidebarOpen: boolean;
  setOverlaySidebarOpen: React.Dispatch<React.SetStateAction<boolean>>;
};

const getOptionLabel = ({ option: untypedOption }: { option?: Option }) => {
  const option = untypedOption as SearchOption;

  return (
    <Link href={option.link}>
      <UnstyledLink href={option.link}>
        <SpacedBetween padding="8px 16px">
          <FlexColumn>
            <Block $style={{ fontSize: '1.05rem' }}>{option.label}</Block>
            <Block marginTop="4px">{option.description}</Block>
          </FlexColumn>
          <VerticalAligned marginLeft="24px">
            <FaLink />
          </VerticalAligned>
        </SpacedBetween>
      </UnstyledLink>
    </Link>
  );
};

export const TopbarMenu = ({
  path,
  searchOptions,
  isSidebarOverlay,
  overlaySidebarOpen,
  setOverlaySidebarOpen,
}: Props) => {
  return (
    <SpacedBetween
      as="nav"
      padding={isSidebarOverlay ? '16px' : '20px 30px'}
      className="topbar menu"
    >
      <Flex>
        <VerticalAligned
          marginRight="8px"
          $style={isSidebarOverlay ? undefined : { display: 'none' }}
        >
          <Button
            size={SIZE.mini}
            kind="tertiary"
            onClick={() => setOverlaySidebarOpen((prev) => !prev)}
          >
            {overlaySidebarOpen ? <Delete /> : <Menu />}
          </Button>
        </VerticalAligned>

        <VerticalAligned>
          <Breadcrumbs path={path} />
        </VerticalAligned>
      </Flex>

      {searchOptions.length > 0 && (
        <VerticalAligned maxWidth="300px" width="100%" marginLeft="16px">
          <Select
            options={searchOptions}
            placeholder="Search"
            type={TYPE.search}
            size={SIZE.compact}
            valueKey="label"
            getOptionLabel={getOptionLabel}
            filterOptions={(options, filterValue) => {
              const query = filterValue.toLowerCase();
              return options.filter((option) => {
                const searchOption = option as SearchOption;
                return (
                  searchOption.label.toLowerCase().includes(query) ||
                  searchOption.description.toLowerCase().includes(query)
                );
              });
            }}
            overrides={{
              ControlContainer: { style: expandBorderRadius('8px') },
              Popover: {
                props: {
                  overrides: { Body: { style: { zIndex: zIndex.SIDEBAR } } },
                },
              },
              DropdownContainer: {
                style: {
                  marginTop: '10px',
                  maxWidth: '500px',
                  width: '100%',
                  minWidth: '300px',
                },
              },
              DropdownListItem: {
                style: {
                  paddingRight: 0,
                  paddingLeft: 0,
                  paddingTop: 0,
                  paddingBottom: 0,
                },
              },
            }}
          />
        </VerticalAligned>
      )}
    </SpacedBetween>
  );
};
