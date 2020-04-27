import React from 'react';
import { useStyletron } from 'baseui';
import { Block } from 'baseui/block';
import { StatefulPopover, PLACEMENT, TRIGGER_TYPE } from 'baseui/popover';
import { StatefulMenu } from 'baseui/menu';
import { Button, SHAPE } from 'baseui/button';
import SsoApi from 'api/sso';
import Router from 'next/router';
import { Menu } from 'baseui/icon';

import Logo from '../Logo';
import GlobalSearch from '../GlobalSearch';

const ITEMS = [{ label: 'Sign out' }];

const Navbar = () => {
  const [css, theme] = useStyletron();

  const onItemSelect = (close: () => void) => async () => {
    await SsoApi.logout();
    Router.push('/login');
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
            <StatefulMenu items={ITEMS} onItemSelect={onItemSelect(close)} />
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
