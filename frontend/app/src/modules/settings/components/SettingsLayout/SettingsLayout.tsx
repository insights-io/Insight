import { Block } from 'baseui/block';
import React from 'react';
import Flex from 'shared/components/Flex';
import { SidebarMenu } from 'modules/settings/components/SidebarMenu';
import { TopbarMenu } from 'modules/settings/components/TopbarMenu';
import { H1 } from 'baseui/typography';
import type {
  SearchOption,
  SettingsLayoutPropsBase,
  SidebarSection,
} from 'modules/settings/types';
import { joinPath } from 'modules/settings/utils';

type Props = Omit<
  SettingsLayoutPropsBase & {
    searchOptions: SearchOption[];
    sidebarSections?: SidebarSection[];
  },
  'header'
> & {
  header?: string;
};

export const SettingsLayout = ({
  header,
  path,
  children,
  searchOptions,
  sidebarSections,
}: Props) => {
  const pathname = joinPath(path);
  const border = '1px solid rgb(231, 225, 236)';

  return (
    <Flex height="100%" flexDirection="column" className="settings">
      <Block $style={{ borderBottom: border }}>
        <TopbarMenu path={path} searchOptions={searchOptions} />
      </Block>

      <Flex flex={1} overflow="scroll">
        {sidebarSections && (
          <Block
            $style={{ borderRight: border }}
            overflow="auto"
            minWidth="200px"
            width="fit-content"
          >
            <SidebarMenu pathname={pathname} sections={sidebarSections} />
          </Block>
        )}

        <Block padding="16px" overflow="auto" width="100%" as="main">
          {header && (
            <H1
              marginBottom={0}
              marginTop={0}
              $style={{
                fontSize: '26px',
                lineHeight: '26px',
                marginBottom: '24px',
              }}
            >
              {header}
            </H1>
          )}
          {children}
        </Block>
      </Flex>
    </Flex>
  );
};
