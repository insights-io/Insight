import React, { forwardRef } from 'react';
import { useStyletron } from 'baseui';
import { Button, SHAPE, SIZE, ButtonProps } from 'baseui/button';
import Link from 'next/link';
import { StatefulTooltip, PLACEMENT } from 'baseui/tooltip';
import { StatefulPopoverProps } from 'baseui/popover';
import { UnstyledLink } from '@rebrowse/elements';
import { Block } from 'baseui/block';

type Props = {
  artwork: React.ReactNode;
  to?: string;
  showText?: boolean;
  text?: string;
  onClick?: ButtonProps['onClick'];
  overrides?: {
    Tooltip?: StatefulPopoverProps;
  };
};

export const NavbarItem = forwardRef<HTMLLIElement, Props>(
  ({ artwork, text, showText, onClick, to, overrides }, ref) => {
    const [css, theme] = useStyletron();

    let content = (
      <Button
        size={SIZE.compact}
        shape={SHAPE.pill}
        onClick={onClick}
        $style={{
          justifyContent: showText ? 'start' : 'center',
          width: '100%',
          height: '100%',
          color: '#d3d3d3',
          ':hover': { color: theme.colors.white },
        }}
      >
        {artwork}
        {showText && text && (
          <Block
            as="span"
            marginLeft={theme.sizing.scale300}
            overflow="hidden"
            className={css({ whiteSpace: 'nowrap', textOverflow: 'ellipsis' })}
          >
            {text}
          </Block>
        )}
      </Button>
    );

    if (to) {
      content = (
        <Link href={to}>
          <UnstyledLink tabIndex={-1} href={to}>
            {content}
          </UnstyledLink>
        </Link>
      );
    }

    return (
      <StatefulTooltip
        content={showText ? undefined : text}
        placement={PLACEMENT.right}
        showArrow
        overrides={{
          ...overrides?.Tooltip,
          Body: { style: { marginLeft: '16px' } },
        }}
      >
        <Block as="li" margin="12px" ref={ref}>
          {content}
        </Block>
      </StatefulTooltip>
    );
  }
);
