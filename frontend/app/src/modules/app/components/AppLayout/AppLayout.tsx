import React, { useMemo, useState, useCallback } from 'react';
import { Block, BlockProps } from 'baseui/block';
import { $StyleProp } from 'styletron-react';
import useWindowSize from 'shared/hooks/useWindowSize';
import { useStyletron } from 'baseui';
import {
  SIDEBAR_WIDTH,
  TOPBAR_HEIGHT,
  EXPANDED_SIDEBAR_WIDTH,
} from 'shared/theme';
import useSidebar from 'modules/app/hooks/useSidebar';
import useOnClickOutside from 'shared/hooks/useOnClickOutside';

import Sidebar from '../Navbar/Sidebar';
import Topbar from '../Navbar/Topbar';

type Props = {
  children: React.ReactNode;
  overrides?: {
    Root?: {
      style?: $StyleProp<BlockProps>;
    };
    MainContent?: {
      style?: $StyleProp<BlockProps>;
    };
  };
};

const AppLayout = ({ children, overrides }: Props) => {
  const { width = 0 } = useWindowSize();
  const [_css, theme] = useStyletron();
  const renderTopbar = width < theme.breakpoints.medium;
  const { collapseSidebar, ...sidebarProps } = useSidebar();
  const [sidebarVisible, setSidebarVisible] = useState(false);

  const handleOnClickOutside = useCallback(() => {
    if (renderTopbar) {
      if (sidebarVisible) {
        setSidebarVisible(false);
      }
    } else {
      collapseSidebar();
    }
  }, [sidebarVisible, renderTopbar, collapseSidebar, setSidebarVisible]);

  const sidebarRef = useOnClickOutside<HTMLDivElement>(handleOnClickOutside);

  const onSidebarMenuClick = useCallback(() => {
    setSidebarVisible((prev) => !prev);
  }, []);

  const topbar = renderTopbar ? (
    <Topbar onMenuClick={onSidebarMenuClick} />
  ) : null;

  let sidebar: React.ReactNode = (
    <Sidebar
      ref={sidebarRef}
      width={renderTopbar ? EXPANDED_SIDEBAR_WIDTH : sidebarProps.width}
      expanded={renderTopbar ? true : sidebarProps.expanded}
      renderLogo={!renderTopbar}
      onCollapseItemClick={
        renderTopbar ? undefined : sidebarProps.onCollapseItemClick
      }
      style={
        renderTopbar
          ? { top: TOPBAR_HEIGHT, height: `calc(100% - ${TOPBAR_HEIGHT})` }
          : undefined
      }
    />
  );
  if (renderTopbar && !sidebarVisible) {
    sidebar = null;
  }

  const rootStyles = useMemo(() => {
    return {
      flexDirection: renderTopbar ? 'column' : undefined,
      ...overrides?.Root?.style,
    } as const;
  }, [renderTopbar, overrides?.Root?.style]);

  const contentContainerStyle = useMemo(() => {
    return {
      boxSizing: 'border-box',
      left: renderTopbar ? 0 : SIDEBAR_WIDTH,
      top: renderTopbar ? TOPBAR_HEIGHT : 0,
      width: renderTopbar ? '100%' : `calc(100% - ${SIDEBAR_WIDTH})`,
      height: renderTopbar ? `calc(100% - ${TOPBAR_HEIGHT})` : '100%',
      ...overrides?.MainContent?.style,
    } as const;
  }, [renderTopbar, overrides?.MainContent?.style]);

  return (
    <Block height="100%" display="flex" $style={rootStyles}>
      {topbar}
      {sidebar}
      <Block
        display="flex"
        flexDirection="column"
        flex="1"
        position="absolute"
        overflow="hidden"
        $style={contentContainerStyle}
      >
        {children}
      </Block>
    </Block>
  );
};

export default AppLayout;
