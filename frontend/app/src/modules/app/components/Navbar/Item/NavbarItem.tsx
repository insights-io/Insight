import React from 'react';
import { useStyletron } from 'baseui';
import { Button, SHAPE, ButtonProps } from 'baseui/button';

type Props = {
  artwork: React.ReactNode;
  showText?: boolean;
  text?: string;
  onClick?: ButtonProps['onClick'];
};

const NavbarItem = ({ artwork, text, showText, ...rest }: Props) => {
  const [css, theme] = useStyletron();
  const { scale300: marginLeft } = theme.sizing;

  return (
    <Button
      size="mini"
      shape={SHAPE.pill}
      $style={{
        justifyContent: showText ? 'start' : 'center',
        width: '100%',
      }}
      {...rest}
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
};

export default React.memo(NavbarItem);
