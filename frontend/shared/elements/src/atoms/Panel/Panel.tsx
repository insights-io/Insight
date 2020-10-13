import React, { forwardRef } from 'react';
import { Block, BlockProps } from 'baseui/block';
import { useStyletron } from 'baseui';

type ItemProps = Omit<BlockProps, 'padding' | 'className'>;

const PANEL_BORDER = '1px solid rgb(198, 190, 207)';

const Header = forwardRef<HTMLDivElement, ItemProps>((props, ref) => {
  const [css, theme] = useStyletron();

  return (
    <Block
      {...props}
      ref={ref}
      className={css({
        padding: theme.sizing.scale600,
        textTransform: 'uppercase',
        color: theme.colors.primary700,
        borderBottom: PANEL_BORDER,
        background: theme.colors.primary50,
        borderTopRightRadius: theme.sizing.scale200,
        borderTopLeftRadius: theme.sizing.scale200,
        fontSize: '13px',
        fontWeight: 600,
      })}
    />
  );
});

const Item = forwardRef<HTMLDivElement, ItemProps>((props, ref) => {
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

type Props = Omit<BlockProps, 'className'>;

export const Panel = (props: Props) => {
  const [css, theme] = useStyletron();

  return (
    <Block
      {...props}
      className={css({
        borderRadius: theme.sizing.scale200,
        boxShadow: 'rgba(37, 11, 54, 0.04) 0px 2px 0px',
        border: PANEL_BORDER,
      })}
    />
  );
};

Panel.Header = Header;
Panel.Item = Item;
