import React, { forwardRef } from 'react';
import { useStyletron } from 'baseui';
import { Block, BlockProps } from 'baseui/block';

import { PANEL_BORDER } from './styles';

type HeaderProps = Omit<BlockProps, 'padding' | 'className'>;

export const Header = forwardRef<HTMLDivElement, HeaderProps>((props, ref) => {
  const [css, theme] = useStyletron();

  return (
    <Block
      {...props}
      ref={ref}
      className={css({
        padding: theme.sizing.scale600,
        textTransform: 'uppercase',
        color: theme.colors.primary700,
        borderBottom: PANEL_BORDER,
        background: theme.colors.primary50,
        borderTopRightRadius: theme.sizing.scale200,
        borderTopLeftRadius: theme.sizing.scale200,
        fontSize: '13px',
        fontWeight: 600,
      })}
    />
  );
});
