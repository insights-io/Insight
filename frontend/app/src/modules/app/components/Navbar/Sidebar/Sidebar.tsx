import React, { useMemo } from 'react';
import { useStyletron } from 'baseui';
import { Block, BlockProps } from 'baseui/block';
import { OnItemSelect, StatefulMenu } from 'baseui/menu';
import AuthApi from 'api/auth';
import Router from 'next/router';
import NavbarItem from 'modules/app/components/Navbar/Item';
import { FaUser, FaListUl, FaInfo } from 'react-icons/fa';
import { ChevronLeft, ChevronRight } from 'baseui/icon';
import { StyleObject } from 'styletron-react';
import { StatefulTooltip, PLACEMENT } from 'baseui/tooltip';
import FlexColumn from 'shared/components/FlexColumn';

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
              AuthApi.sso.session.logout().then((_) => Router.push('/login')),
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
      <FlexColumn
        ref={ref}
        as="nav"
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
            artwork={<FaListUl />}
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
          marginBottom={theme.sizing.scale500}
          padding={0}
        >
          <StatefulTooltip
            triggerType="click"
            placement={PLACEMENT.right}
            showArrow={false}
            overrides={{
              Body: {
                style: {
                  zIndex: 1,
                },
              },
              Inner: {
                style: {
                  paddingLeft: 0,
                  paddingRight: 0,
                  paddingBottom: 0,
                  paddingTop: 0,
                },
              },
            }}
            content={({ close }) => (
              <StatefulMenu
                items={menuItems}
                onItemSelect={onItemSelect(close)}
              />
            )}
          >
            <Block>
              <NavbarItem
                artwork={<FaUser id="account-settings" />}
                text="Account settings"
                showText={expanded}
              />
            </Block>
          </StatefulTooltip>

          {onCollapseItemClick && (
            <NavbarItem
              artwork={
                expanded ? (
                  <ChevronLeft
                    overrides={{ Svg: { props: { id: 'sidebar--togle' } } }}
                  />
                ) : (
                  <ChevronRight
                    overrides={{ Svg: { props: { id: 'sidebar--togle' } } }}
                  />
                )
              }
              showText={expanded}
              text={expanded ? 'Collapse' : 'Expand'}
              onClick={onCollapseItemClick}
            />
          )}
        </Block>
      </FlexColumn>
    );
  }
);

export default React.memo(Sidebar);
