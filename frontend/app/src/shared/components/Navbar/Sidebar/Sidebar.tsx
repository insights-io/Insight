import React, { forwardRef } from 'react';
import { useStyletron } from 'baseui';
import { Block, BlockProps } from 'baseui/block';
import { NavbarItem } from 'shared/components/Navbar/Item';
import { FaListUl, FaInfo } from 'react-icons/fa';
import { ChevronLeft, ChevronRight } from 'baseui/icon';
import { StyleObject } from 'styletron-react';
import { FlexColumn } from '@rebrowse/elements';
import { INDEX_PAGE, SESSIONS_PAGE } from 'shared/constants/routes';
import type { Organization, User } from '@rebrowse/types';
import * as zIndex from 'shared/constants/zIndex';
import { NavbarBanner } from 'shared/components/Navbar/Banner';

type Props = {
  width: BlockProps['width'];
  expanded: boolean;
  onCollapseItemClick?: () => void;
  renderLogo?: boolean;
  style?: StyleObject;
  user: User;
  organization: Organization;
};

export const Sidebar = forwardRef<HTMLDivElement, Props>(
  (
    {
      width,
      expanded,
      onCollapseItemClick,
      renderLogo,
      style,
      user,
      organization,
    },
    ref
  ) => {
    const [_css, theme] = useStyletron();

    return (
      <FlexColumn
        ref={ref}
        as="nav"
        overflow="hidden"
        position="fixed"
        className="sidebar"
        height="100%"
        width={width}
        color={theme.colors.white}
        backgroundColor={theme.colors.black}
        $style={{ zIndex: zIndex.SIDEBAR, ...style }}
      >
        <Block as="ul" $style={{ listStyle: 'none' }} margin={0} padding={0}>
          <Block margin="12px" as="li">
            <NavbarBanner
              expanded={expanded}
              organizationName={organization.name}
              organizationAvatar={organization.avatar}
              user={user}
              theme={theme}
            />
          </Block>
          {renderLogo && (
            <NavbarItem
              to={INDEX_PAGE}
              artwork={<FaInfo />}
              text="Insights"
              showText={expanded}
            />
          )}
          <NavbarItem
            to={SESSIONS_PAGE}
            artwork={<FaListUl />}
            text="Sessions"
            showText={expanded}
          />
        </Block>

        <Block
          as="ul"
          marginTop="auto"
          marginBottom={0}
          padding={0}
          $style={{ listStyle: 'none' }}
        >
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
