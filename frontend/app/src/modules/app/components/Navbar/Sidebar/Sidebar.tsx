import React, { useMemo } from 'react';
import { useStyletron } from 'baseui';
import { Block, BlockProps } from 'baseui/block';
import { StatefulPopover, PLACEMENT, TRIGGER_TYPE } from 'baseui/popover';
import { OnItemSelect, StatefulMenu } from 'baseui/menu';
import AuthApi from 'api/auth';
import Router from 'next/router';
import NavbarItem from 'modules/app/components/Navbar/Item';
import { FaUser } from 'react-icons/fa';
import { ChevronLeft, ChevronRight } from 'baseui/icon';
import { StyleObject } from 'styletron-react';

type Props = {
  width: BlockProps['width'];
  expanded: boolean;
  onCollapseItemClick?: () => void;
  style?: StyleObject;
};

const Sidebar = ({ width, expanded, onCollapseItemClick, style }: Props) => {
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
      display="flex"
      overflow="hidden"
      position="fixed"
      height="100%"
      width={width}
      color={theme.colors.white}
      backgroundColor={theme.colors.black}
      $style={{
        zIndex: 1,
        transitionDuration: theme.animation.timing100,
        transitionProperty: 'width',
        transitionTimingFunction: theme.animation.easeInOutQuinticCurve,
        ...style,
      }}
    >
      <Block
        display="flex"
        justifyContent="center"
        flexDirection="column"
        marginTop="auto"
        width="100%"
        marginBottom={theme.sizing.scale400}
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
            <NavbarItem
              artwork={<FaUser />}
              text="Account settings"
              showText={expanded}
            />
          </Block>
        </StatefulPopover>

        {onCollapseItemClick && (
          <NavbarItem
            artwork={expanded ? <ChevronLeft /> : <ChevronRight />}
            showText={expanded}
            text="Collapse"
            onClick={onCollapseItemClick}
          />
        )}
      </Block>
    </Block>
  );
};

export default React.memo(Sidebar);
