import React from 'react';
import { Block } from 'baseui/block';
import { TOPBAR_HEIGHT } from 'shared/theme';
import { useStyletron } from 'baseui';
import { FaInfo, FaBars } from 'react-icons/fa';
import { PLACEMENT } from 'baseui/tooltip';

import NavbarItem from '../Item';

type Props = {
  onMenuClick: (event: React.MouseEvent<HTMLButtonElement, MouseEvent>) => void;
};

const NAVBAR_ITEM_OVERRIDES = {
  Tooltip: {
    placement: PLACEMENT.bottomRight,
  },
};

const Topbar = ({ onMenuClick }: Props) => {
  const [_css, theme] = useStyletron();

  return (
    <Block
      width="100%"
      display="flex"
      height={TOPBAR_HEIGHT}
      color={theme.colors.white}
      backgroundColor={theme.colors.black}
    >
      <Block
        height="100%"
        display="flex"
        justifyContent="center"
        as="ul"
        margin={0}
        padding={0}
        $style={{ listStyle: 'none' }}
      >
        <NavbarItem
          artwork={<FaBars id="toggle-sidebar" />}
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
      </Block>
    </Block>
  );
};

export default React.memo(Topbar);
