import React from 'react';
import { Block, BlockProps } from 'baseui/block';
import { useStyletron } from 'baseui';

import { Item } from './Item';
import { PANEL_BORDER } from './styles';
import { Header } from './Header';
import { Label } from './Label';

type Props = Omit<BlockProps, 'className'>;

export const Panel = (props: Props) => {
  const [css, theme] = useStyletron();

  return (
    <Block
      {...props}
      className={css({
        borderRadius: theme.sizing.scale200,
        boxShadow: 'rgba(37, 11, 54, 0.04) 0px 2px 0px',
        border: PANEL_BORDER,
      })}
    />
  );
};

Panel.Header = Header;
Panel.Item = Item;
Panel.Label = Label;
