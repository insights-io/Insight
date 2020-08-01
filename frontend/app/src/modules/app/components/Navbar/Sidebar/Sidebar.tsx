import React, { useMemo } from 'react';
import { useStyletron } from 'baseui';
import { Block, BlockProps } from 'baseui/block';
import { OnItemSelect, StatefulMenu } from 'baseui/menu';
import AuthApi from 'api/auth';
import Router from 'next/router';
import NavbarItem from 'modules/app/components/Navbar/Item';
import { FaUser, FaChartArea, FaInfo } from 'react-icons/fa';
import { ChevronLeft, ChevronRight } from 'baseui/icon';
import { StyleObject } from 'styletron-react';

type Props = {
  width: BlockProps['width'];
  expanded: boolean;
  onCollapseItemClick?: () => void;
  renderLogo?: boolean;
  style?: StyleObject;
};

const Sidebar = React.forwardRef<HTMLDivElement, Props>(
  ({ width, expanded, onCollapseItemClick, renderLogo, style }, ref) => {
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
        ref={ref}
        as="nav"
        display="flex"
        overflow="hidden"
        position="fixed"
        flexDirection="column"
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
        <Block as="ul" $style={{ listStyle: 'none' }} margin={0} padding={0}>
          {renderLogo && (
            <NavbarItem
              to="/"
              artwork={<FaInfo />}
              text="Insights"
              showText={expanded}
            />
          )}
          <NavbarItem
            to="/sessions"
            artwork={<FaChartArea />}
            text="Sessions"
            showText={expanded}
          />
        </Block>

        <Block
          marginTop="auto"
          as="ul"
          $style={{ listStyle: 'none' }}
          marginLeft={0}
          marginRight={0}
          marginBottom={theme.sizing.scale400}
          padding={0}
        >
          <NavbarItem
            artwork={<FaUser id="account-settings" />}
            text="Account settings"
            showText={expanded}
            overrides={{
              Tooltip: {
                overrides: {
                  Inner: {
                    style: {
                      paddingLeft: 0,
                      paddingRight: 0,
                      paddingTop: 0,
                      paddingBottom: 0,
                    },
                  },
                },
                showArrow: false,
                content: ({ close }) => (
                  <StatefulMenu
                    items={menuItems}
                    onItemSelect={onItemSelect(close)}
                  />
                ),
              },
            }}
          />

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
  }
);

export default React.memo(Sidebar);
