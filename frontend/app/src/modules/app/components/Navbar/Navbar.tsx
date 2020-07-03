import React, { useMemo } from 'react';
import { useStyletron } from 'baseui';
import { Block } from 'baseui/block';
import { StatefulPopover, PLACEMENT, TRIGGER_TYPE } from 'baseui/popover';
import { StatefulMenu, OnItemSelect } from 'baseui/menu';
import { Button, SHAPE } from 'baseui/button';
import SsoApi from 'api/sso';
import Router from 'next/router';
import { Menu } from 'baseui/icon';

import Logo from '../Logo';
import GlobalSearch from '../GlobalSearch';

const Navbar = () => {
  const [css, theme] = useStyletron();

  const menuItems = useMemo(() => {
    return [
      {
        label: 'Sign out',
        handler: async () => {
          await SsoApi.logout();
          Router.push('/login');
        },
      },
      {
        label: 'Sign out of all devices',
        handler: async () => {
          await SsoApi.logoutFromAllDevices();
          Router.push('/login');
        },
      },
    ];
  }, []);

  type MenuItem = typeof menuItems[number];

  const onItemSelect = (close: () => void): OnItemSelect => async ({
    item,
    event: _event,
  }) => {
    await (item as MenuItem).handler();
    close();
  };

  return (
    <nav
      className={css({
        padding: theme.sizing.scale600,
        borderBottom: `1px solid ${theme.colors.primary200}`,
      })}
    >
      <Block display="flex" justifyContent="space-between">
        <Block display="flex">
          <Block display="flex" flexDirection="column" justifyContent="center">
            <Logo />
          </Block>
          <GlobalSearch />
        </Block>
        <StatefulPopover
          focusLock
          placement={PLACEMENT.bottomLeft}
          triggerType={TRIGGER_TYPE.click}
          content={({ close }) => (
            <StatefulMenu
              items={menuItems}
              onItemSelect={onItemSelect(close)}
            />
          )}
        >
          <Button size="mini" shape={SHAPE.pill}>
            <Menu />
          </Button>
        </StatefulPopover>
      </Block>
    </nav>
  );
};

export default React.memo(Navbar);
