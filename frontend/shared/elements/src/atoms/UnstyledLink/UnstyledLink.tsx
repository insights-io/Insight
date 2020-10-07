import React, { forwardRef } from 'react';
import { Block, BlockProps } from 'baseui/block';

type Props = React.DetailedHTMLProps<
  React.AnchorHTMLAttributes<HTMLAnchorElement>,
  HTMLAnchorElement
> &
  Omit<BlockProps, 'as' | 'ref'>;

export const UnstyledLink = forwardRef<HTMLAnchorElement, Props>(
  ({ $style, ...rest }, ref) => {
    return (
      <Block
        as="a"
        $style={{ textDecoration: 'none', color: 'inherit', ...$style }}
        {...rest}
        ref={ref}
      />
    );
  }
);
