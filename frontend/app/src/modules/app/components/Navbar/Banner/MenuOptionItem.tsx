import React from 'react';
import { StyledListItem } from 'baseui/menu';
import Link from 'next/link';
import { UnstyledLink } from '@insight/elements';

type Item = {
  label: string;
  link: string;
  onClick?: (event: React.MouseEvent<HTMLAnchorElement, MouseEvent>) => void;
};

type Props = {
  item: Item;
};

export const MenuOptionItem = ({ item, ...rest }: Props) => {
  return (
    <Link href={item.link}>
      <UnstyledLink href={item.link} onClick={item.onClick}>
        <StyledListItem {...rest} />
      </UnstyledLink>
    </Link>
  );
};
