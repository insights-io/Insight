import React from 'react';
import { useStyletron } from 'baseui';
import { Button, SHAPE, ButtonProps } from 'baseui/button';
import Link from 'next/link';
import { StatefulTooltip, PLACEMENT } from 'baseui/tooltip';
import { StatefulPopoverProps } from 'baseui/popover';

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

const NavbarItem = React.forwardRef<HTMLLIElement, Props>(
  ({ artwork, text, showText, onClick, to, overrides }, ref) => {
    const [css, theme] = useStyletron();
    const { scale300: marginLeft } = theme.sizing;

    let content = (
      <Button
        size="mini"
        shape={SHAPE.pill}
        onClick={onClick}
        $style={{
          justifyContent: showText ? 'start' : 'center',
          width: '100%',
          height: '100%',
          paddingTop: theme.sizing.scale400,
          paddingBottom: theme.sizing.scale400,
        }}
      >
        {artwork}
        {showText && text && (
          <span
            className={css({
              marginLeft,
              whiteSpace: 'nowrap',
              overflow: 'hidden',
              textOverflow: 'ellipsis',
            })}
          >
            {text}
          </span>
        )}
      </Button>
    );

    if (to) {
      content = (
        <Link href={to}>
          <a tabIndex={-1}>{content}</a>
        </Link>
      );
    }

    return (
      <StatefulTooltip
        content={showText ? undefined : text}
        placement={PLACEMENT.right}
        onMouseEnterDelay={1000}
        showArrow
        {...overrides?.Tooltip}
      >
        <li ref={ref}>{content}</li>
      </StatefulTooltip>
    );
  }
);

export default React.memo(NavbarItem);
