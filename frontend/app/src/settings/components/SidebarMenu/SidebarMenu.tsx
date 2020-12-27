import React, { forwardRef, memo } from 'react';
import { useStyletron } from 'baseui';
import { Block, BlockProps } from 'baseui/block';
import Link from 'next/link';
import type { StyleObject } from 'styletron-react';
import type { Theme } from 'baseui/theme';
import type { SidebarSection } from 'settings/types';

const VERTICAL_MARGIN_SECTIONS = '20px';
const PADDING_LEFT = '30px';

const SidebarMenuHeader = forwardRef<HTMLElement, BlockProps>((props, ref) => {
  return (
    <Block
      {...props}
      ref={ref}
      marginBottom={VERTICAL_MARGIN_SECTIONS}
      $style={{
        textTransform: 'uppercase',
        fontSize: '1.05rem',
      }}
    />
  );
});

type ListItemProps = {
  to: string;
  children: React.ReactNode;
  isActive: boolean;
  onClick?: (event: React.MouseEvent<HTMLAnchorElement, MouseEvent>) => void;
  css: (arg: StyleObject) => string;
  theme: Theme;
};

const SidebarMenuListItem = forwardRef<HTMLLIElement, ListItemProps>(
  ({ to, children, css, theme, isActive, onClick }, ref) => {
    const { primary500: defaultColor, primary700: activeColor } = theme.colors;

    const styles = css({
      position: 'relative',
      lineHeight: '30px',
      fontSize: '1.05rem',
      textDecoration: 'none',
      color: isActive ? activeColor : defaultColor,
      ':hover': {
        color: activeColor,
      },
      '::before': isActive
        ? {
            content: '""',
            display: 'block',
            position: 'absolute',
            left: `-${PADDING_LEFT}`,
            width: '4px',
            background: theme.colors.accent300,
            borderRadius: '0px 2px 2px 0px',
            top: '1px',
            bottom: '1px',
          }
        : undefined,
    });

    return (
      <li ref={ref}>
        <Link href={to}>
          <a className={styles} onClick={onClick} href={to}>
            {children}
          </a>
        </Link>
      </li>
    );
  }
);

type Props = {
  sections: SidebarSection[];
  pathname: string;
  onItemClick?: (
    link: string,
    event: React.MouseEvent<HTMLAnchorElement, MouseEvent>
  ) => void;
};

export const SidebarMenu = memo(
  ({ sections, pathname, onItemClick }: Props) => {
    const [css, theme] = useStyletron();

    return (
      <Block
        as="nav"
        className="sidebar menu"
        height="100%"
        padding={`30px 16px 30px ${PADDING_LEFT}`}
      >
        {sections.map((section, index) => {
          const { header, items } = section;

          return (
            <Block
              key={header}
              marginTop={index === 0 ? 0 : VERTICAL_MARGIN_SECTIONS}
            >
              <SidebarMenuHeader>{header}</SidebarMenuHeader>
              <ul className={css({ listStyle: 'none', margin: 0, padding: 0 })}>
                {items.map((sectionItem) => {
                  const isActive = pathname.startsWith(sectionItem.link);

                  return (
                    <SidebarMenuListItem
                      key={sectionItem.link}
                      to={sectionItem.link}
                      isActive={isActive}
                      theme={theme}
                      css={css}
                      onClick={
                        onItemClick
                          ? (event) => onItemClick(sectionItem.link, event)
                          : undefined
                      }
                    >
                      {sectionItem.text}
                    </SidebarMenuListItem>
                  );
                })}
              </ul>
            </Block>
          );
        })}
      </Block>
    );
  }
);
