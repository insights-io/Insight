import React from 'react';
import { TOPBAR_HEIGHT } from 'shared/theme';
import { useStyletron } from 'baseui';
import { FaInfo, FaBars, FaTimes } from 'react-icons/fa';
import { PLACEMENT } from 'baseui/tooltip';
import { Flex } from '@rebrowse/elements';
import { NavbarItem } from 'shared/components/Navbar/Item';

export type Props = {
  onMenuClick: (event: React.MouseEvent<HTMLButtonElement, MouseEvent>) => void;
  sidebarVisible: boolean;
};

const NAVBAR_ITEM_OVERRIDES = {
  Tooltip: {
    placement: PLACEMENT.bottomRight,
  },
};

export const NavbarTopbar = ({ onMenuClick, sidebarVisible }: Props) => {
  const [_css, theme] = useStyletron();
  const ToggleSidebarIcon = sidebarVisible ? FaTimes : FaBars;

  return (
    <Flex
      width="100%"
      height={TOPBAR_HEIGHT}
      color={theme.colors.white}
      backgroundColor={theme.colors.black}
    >
      <Flex
        height="100%"
        justifyContent="center"
        as="ul"
        margin={0}
        padding={0}
        $style={{ listStyle: 'none' }}
      >
        <NavbarItem
          artwork={<ToggleSidebarIcon id="toggle-sidebar" />}
          onClick={onMenuClick}
          text="Open sidebar"
          overrides={NAVBAR_ITEM_OVERRIDES}
        />
        <NavbarItem
          to="/"
          artwork={<FaInfo />}
          text="Insights"
          overrides={NAVBAR_ITEM_OVERRIDES}
          showText
        />
      </Flex>
    </Flex>
  );
};
