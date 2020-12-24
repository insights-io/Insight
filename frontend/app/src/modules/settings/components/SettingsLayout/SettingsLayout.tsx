import { Block } from 'baseui/block';
import React, { useState } from 'react';
import { Flex, FlexColumn } from '@rebrowse/elements';
import { SidebarMenu } from 'modules/settings/components/SidebarMenu';
import { TopbarMenu } from 'modules/settings/components/TopbarMenu';
import { H1 } from 'baseui/typography';
import type {
  SearchOption,
  SettingsLayoutPropsBase,
  SidebarSection,
} from 'modules/settings/types';
import useWindowSize from 'shared/hooks/useWindowSize';
import { useStyletron } from 'baseui';
import * as zIndex from 'shared/constants/zIndex';
import { joinPath } from 'shared/components/Breadcrumbs/utils';

import { ContentMask } from '../TopbarMenu/ContentMask';

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
  const [_css, theme] = useStyletron();
  const pathname = joinPath(path);
  const border = '1px solid rgb(231, 225, 236)';
  const { width = 0 } = useWindowSize();
  const isSidebarOverlay = width < theme.breakpoints.medium;
  const [overlaySidebarOpen, setOverlaySidebarOpen] = useState(false);

  return (
    <FlexColumn height="100%" className="settings">
      <Block $style={{ borderBottom: border }}>
        <TopbarMenu
          path={path}
          searchOptions={searchOptions}
          isSidebarOverlay={isSidebarOverlay && Boolean(sidebarSections)}
          setOverlaySidebarOpen={setOverlaySidebarOpen}
          overlaySidebarOpen={overlaySidebarOpen}
        />
      </Block>

      <Flex flex={1} overflow="scroll" position="relative">
        <ContentMask
          active={overlaySidebarOpen && isSidebarOverlay}
          onClick={() => setOverlaySidebarOpen(false)}
        />

        {sidebarSections && (!isSidebarOverlay || overlaySidebarOpen) && (
          <Block
            $style={{
              borderRight: border,
              ...(isSidebarOverlay
                ? {
                    position: 'absolute',
                    top: 0,
                    bottom: 0,
                    background: theme.colors.white,
                    zIndex: zIndex.SETTINGS_LAYOUT_SIDEBAR,
                  }
                : undefined),
            }}
            overflow="auto"
            minWidth="200px"
            width="fit-content"
          >
            <SidebarMenu pathname={pathname} sections={sidebarSections} />
          </Block>
        )}

        <Block
          padding={theme.sizing.scale600}
          overflow="auto"
          width="100%"
          as="section"
        >
          {header && (
            <H1
              marginBottom={0}
              marginTop={0}
              $style={{
                fontSize: '26px',
                lineHeight: '26px',
                marginBottom: theme.sizing.scale800,
              }}
            >
              {header}
            </H1>
          )}
          {children}
        </Block>
      </Flex>
    </FlexColumn>
  );
};
