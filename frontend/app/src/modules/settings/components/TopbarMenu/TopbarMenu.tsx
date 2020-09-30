import React from 'react';
import { Block } from 'baseui/block';
import { Breadcrumbs } from 'baseui/breadcrumbs';
import Link from 'next/link';
import { Select, SIZE, TYPE, Option } from 'baseui/select';
import {
  FlexColumn,
  VerticalAligned,
  SpacedBetween,
  UnstyledLink,
} from '@insight/elements';
import { FaLink } from 'react-icons/fa';
import { joinSegments } from 'modules/settings/utils';
import type { Path, SearchOption } from 'modules/settings/types';

type Props = {
  path: Path;
  searchOptions: SearchOption[];
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

export const TopbarMenu = ({ path, searchOptions }: Props) => {
  return (
    <SpacedBetween as="nav" padding="20px 30px" className="topbar menu">
      <VerticalAligned>
        <Breadcrumbs>
          {path.map((pathPart, index) => {
            const link = joinSegments(
              path.slice(0, index + 1).map((p) => p.segment)
            );

            return (
              <Link key={link} href={link}>
                <UnstyledLink href={link}>{pathPart.text}</UnstyledLink>
              </Link>
            );
          })}
        </Breadcrumbs>
      </VerticalAligned>

      {searchOptions.length > 0 && (
        <Block maxWidth="300px" width="100%">
          <Select
            options={searchOptions}
            placeholder="Search"
            type={TYPE.search}
            size={SIZE.compact}
            valueKey="label"
            getOptionLabel={getOptionLabel}
            overrides={{
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
        </Block>
      )}
    </SpacedBetween>
  );
};
