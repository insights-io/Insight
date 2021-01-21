import React from 'react';
import { useStyletron } from 'baseui';
import { Block } from 'baseui/block';
import { FlexColumn, VerticalAligned } from '@rebrowse/elements';
import type { Theme } from 'baseui/theme';
import type { StyleObject } from 'styletron-react';

type Props = {
  children: (styletron: {
    theme: Theme;
    css: (s: StyleObject) => string;
  }) => JSX.Element;
};

export const AccountsLayout = ({ children }: Props) => {
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
