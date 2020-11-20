import React, { forwardRef } from 'react';
import { StyledListItem } from 'baseui/menu';
import Link from 'next/link';
import { UnstyledLink } from '@rebrowse/elements';

type Item = {
  label: string;
  link: string;
  onClick?: (event: React.MouseEvent<HTMLAnchorElement, MouseEvent>) => void;
};

type Props = {
  item: Item;
};

export const MenuOptionItem = forwardRef<HTMLLIElement, Props>(
  ({ item, ...rest }, ref) => {
    return (
      <Link href={item.link}>
        <UnstyledLink href={item.link} onClick={item.onClick}>
          <StyledListItem ref={ref} {...rest} />
        </UnstyledLink>
      </Link>
    );
  }
);
