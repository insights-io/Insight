import React from 'react';
import { Block } from 'baseui/block';
import { InputProps } from 'baseui/input';
import { Pagination, PaginationProps } from 'baseui/pagination';
import { useStyletron } from 'baseui';
import { Theme } from 'baseui/theme';

import { Panel } from '../../atoms/Panel';
import { SpacedBetween } from '../../atoms/SpacedBetween';
import { Input } from '../../inputs/Input';
import { Flex } from '../../atoms/Flex';

type Props<T> = {
  header: string;
  items: T[];
  children: (item: T) => React.ReactNode;
  itemKey: (item: T) => string;
  search?: InputProps;
  pagination?: PaginationProps;
};

type HeaderProps = InputProps & { children?: React.ReactNode; theme: Theme };

const Header = ({ theme, children, ...search }: HeaderProps) => {
  return (
    <SpacedBetween marginBottom={theme.sizing.scale600}>
      <Input {...search} />
      {children}
    </SpacedBetween>
  );
};

type BodyProps<T> = Pick<Props<T>, 'items' | 'header' | 'children' | 'itemKey'>;

const Body = <T,>({ items, itemKey, header, children }: BodyProps<T>) => {
  return (
    <Panel>
      <Panel.Header>{header}</Panel.Header>
      <Block as="ul" padding="0" margin="0">
        {items.map((item) => {
          return (
            <Panel.Item
              key={itemKey(item)}
              as="li"
              $style={{ listStyle: 'none' }}
            >
              {children(item)}
            </Panel.Item>
          );
        })}
      </Block>
    </Panel>
  );
};

type FooterProps = PaginationProps & { theme: Theme };

const Footer = ({ theme, ...pagination }: FooterProps) => {
  return (
    <Flex justifyContent="flex-end" marginTop={theme.sizing.scale600}>
      <Pagination {...pagination} />
    </Flex>
  );
};

export const Table = <T,>({
  items,
  children,
  search,
  pagination,
  header,
  itemKey,
}: Props<T>) => {
  const [_css, theme] = useStyletron();
  return (
    <Block>
      {search && <Header {...search} theme={theme} />}
      <Body items={items} header={header} itemKey={itemKey}>
        {children}
      </Body>
      {pagination && <Footer {...pagination} theme={theme} />}
    </Block>
  );
};

Table.Header = Header;
Table.Body = Body;
Table.Footer = Footer;
