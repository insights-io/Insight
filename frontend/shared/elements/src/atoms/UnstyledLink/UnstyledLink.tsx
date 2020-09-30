import { Block } from 'baseui/block';
import React, { forwardRef } from 'react';

type Props = React.DetailedHTMLProps<
  React.AnchorHTMLAttributes<HTMLAnchorElement>,
  HTMLAnchorElement
>;

export const UnstyledLink = forwardRef<HTMLAnchorElement, Props>(
  (props, ref) => {
    return (
      <Block
        as="a"
        $style={{ textDecoration: 'none', color: 'inherit' }}
        {...props}
        ref={ref}
      />
    );
  }
);
