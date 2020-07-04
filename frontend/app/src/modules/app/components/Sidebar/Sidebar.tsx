import React, { useMemo } from 'react';
import { Block } from 'baseui/block';
import { useStyletron } from 'baseui';
import { StatefulPopover, PLACEMENT, TRIGGER_TYPE } from 'baseui/popover';
import { StatefulMenu, OnItemSelect } from 'baseui/menu';
import { Button, SHAPE } from 'baseui/button';
import { Menu } from 'baseui/icon';
import SsoApi from 'api/sso';
import Router from 'next/router';

import Logo from '../Logo';

const Sidebar = () => {
  const [_css, theme] = useStyletron();

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
          handler: () => SsoApi.logout().then((_) => Router.push('/login')),
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
      color={theme.colors.white}
      backgroundColor={theme.colors.black}
      display="flex"
      flexDirection="column"
      height="100%"
      width="48px"
      overflow="hidden"
      $style={{ boxSizing: 'border-box' }}
    >
      <Block
        display="flex"
        justifyContent="center"
        padding={theme.sizing.scale200}
      >
        <Logo />
      </Block>
      <Block marginTop="auto">
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
          <Block
            display="flex"
            justifyContent="center"
            padding={theme.sizing.scale200}
          >
            <Button size="mini" shape={SHAPE.pill}>
              <Menu />
            </Button>
          </Block>
        </StatefulPopover>
      </Block>
    </Block>
  );
};

export default React.memo(Sidebar);
