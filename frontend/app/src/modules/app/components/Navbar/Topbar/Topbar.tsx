import React from 'react';
import { Block } from 'baseui/block';
import { TOPBAR_HEIGHT } from 'shared/theme';
import { useStyletron } from 'baseui';
import { Menu } from 'baseui/icon';

import NavbarItem from '../Item';

type Props = {
  onMenuClick: (event: React.MouseEvent<HTMLButtonElement, MouseEvent>) => void;
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
      <Block height="100%" display="flex" justifyContent="center">
        <NavbarItem artwork={<Menu />} onClick={onMenuClick} />
      </Block>
    </Block>
  );
};

export default React.memo(Topbar);
