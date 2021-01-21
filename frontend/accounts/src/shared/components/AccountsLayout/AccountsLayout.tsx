import React from 'react';
import { useStyletron } from 'baseui';
import { Block, BlockProps } from 'baseui/block';
import { FlexColumn, VerticalAligned } from '@rebrowse/elements';
import type { Theme } from 'baseui/theme';
import type { StyleObject } from 'styletron-react';
import { H1, H2 } from 'baseui/typography';

type Props = {
  children: (styletron: {
    theme: Theme;
    css: (s: StyleObject) => string;
  }) => JSX.Element;
};

const Layout = ({ children }: Props) => {
  const [css, theme] = useStyletron();

  return (
    <FlexColumn height="100%">
      <VerticalAligned height="100%" padding={theme.sizing.scale600}>
        <Block
          width="100%"
          maxWidth="480px"
          marginLeft="auto"
          marginRight="auto"
          $style={{
            '@media screen and @media screen and (max-width: 600px)': {
              marginLeft: 0,
              marginRight: 0,
              maxWidth: '100%',
              height: '100%',
            },
          }}
        >
          {children({ css, theme })}
        </Block>
      </VerticalAligned>
    </FlexColumn>
  );
};

const Header = React.forwardRef<HTMLHeadingElement, BlockProps>(
  (props, ref) => {
    // TODO: correctly pass $style object and get $theme from it
    const [_css, theme] = useStyletron();
    return (
      <H1
        ref={ref}
        $style={{
          marginBottom: theme.sizing.scale800,
          marginTop: 0,
          fontWeight: 700,
          fontSize: theme.typography.font950.fontSize,
        }}
        {...props}
      />
    );
  }
);

const SubHeader = React.forwardRef<HTMLHeadingElement, BlockProps>(
  (props, ref) => {
    // TODO: correctly pass $style object and get $theme from it
    const [_css, theme] = useStyletron();
    return (
      <H2
        ref={ref}
        color={theme.colors.primary400}
        marginTop={theme.sizing.scale400}
        marginBottom={theme.sizing.scale1000}
        $style={{
          fontSize: theme.typography.font450.fontSize,
          lineHeight: 'normal',
        }}
        {...props}
      />
    );
  }
);

export const AccountsLayout = Object.assign(Layout, {
  Header,
  SubHeader,
});
