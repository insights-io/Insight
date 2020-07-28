import React, { useMemo, useState } from 'react';
import { Block, BlockOverrides } from 'baseui/block';
import { useStyletron } from 'baseui';
import { StatefulPopover, PLACEMENT, TRIGGER_TYPE } from 'baseui/popover';
import { StatefulMenu, OnItemSelect } from 'baseui/menu';
import { Menu, ChevronRight, ChevronLeft } from 'baseui/icon';
import AuthApi from 'api/auth';
import Router from 'next/router';
import { FaUser } from 'react-icons/fa';
import { SIDEBAR_WIDTH, EXPANDED_SIDEBAR_WIDTH } from 'shared/theme';

import Logo from '../Logo';

import NavbarItem from './Item';

type Props = {
  type: 'sidebar' | 'topbar';
  overrides?: {
    Root: BlockOverrides;
  };
};

const Navbar = ({ type, overrides }: Props) => {
  const [expanded, setExpanded] = useState(false);
  const [_css, theme] = useStyletron();
  const isSidebar = type === 'sidebar';
  let width = '100%';
  if (isSidebar) {
    if (expanded) {
      width = EXPANDED_SIDEBAR_WIDTH;
    } else {
      width = SIDEBAR_WIDTH;
    }
  }

  let navbarCollapseItem: React.ReactNode = null;
  if (isSidebar) {
    navbarCollapseItem = (
      <NavbarItem
        artwork={expanded ? <ChevronLeft /> : <ChevronRight />}
        showText={expanded}
        text="Collapse"
        onClick={() => setExpanded((prev) => !prev)}
      />
    );
  }

  const menuItems = useMemo(() => {
    return {
      __ungrouped: [],
      Account: [
        {
          label: 'Account settings',
          handler: () => Router.push('/account/settings'),
        },
        {
          label: 'Sign out',
          handler: () =>
            AuthApi.sso.logout().then((_) => Router.push('/login')),
        },
      ],
    };
  }, []);

  type MenuItem = typeof menuItems.Account[number];

  const onItemSelect = (close: () => void): OnItemSelect => async ({
    item,
    event: _event,
  }) => {
    await (item as MenuItem).handler();
    close();
  };

  return (
    <Block
      as="nav"
      color={theme.colors.white}
      backgroundColor={theme.colors.black}
      display="flex"
      overflow="hidden"
      width={width}
      position="fixed"
      overrides={overrides?.Root}
    >
      <Block display="flex" padding={theme.sizing.scale200}>
        <Logo />
      </Block>
      <Block
        display="flex"
        justifyContent="center"
        flexDirection="column"
        marginTop={isSidebar ? 'auto' : undefined}
      >
        <StatefulPopover
          showArrow
          focusLock
          placement={PLACEMENT.right}
          triggerType={TRIGGER_TYPE.click}
          content={({ close }) => (
            <StatefulMenu
              items={menuItems}
              onItemSelect={onItemSelect(close)}
            />
          )}
        >
          <Block display="flex" padding={theme.sizing.scale200}>
            {isSidebar ? (
              <NavbarItem
                artwork={<FaUser />}
                text="Account settings"
                showText={expanded}
              />
            ) : (
              <NavbarItem artwork={<Menu />} />
            )}
          </Block>
        </StatefulPopover>
        {navbarCollapseItem}
      </Block>
    </Block>
  );
};

export default React.memo(Navbar);
