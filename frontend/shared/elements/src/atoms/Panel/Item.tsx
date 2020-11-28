import React, { forwardRef } from 'react';
import { useStyletron } from 'baseui';
import { Block, BlockProps } from 'baseui/block';

type ItemProps = Omit<BlockProps, 'padding' | 'className'>;

export const Item = forwardRef<HTMLDivElement, ItemProps>((props, ref) => {
  const [css, theme] = useStyletron();
  return (
    <Block
      {...props}
      ref={ref}
      className={css({
        padding: theme.sizing.scale600,
        borderBottom: '1px solid rgb(231, 225, 236);',
        ':last-child': {
          borderBottom: 'none',
        },
      })}
    />
  );
});
