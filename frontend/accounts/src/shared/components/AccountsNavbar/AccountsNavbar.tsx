import React from 'react';
import { Block } from 'baseui/block';
import { H6 } from 'baseui/typography';
import {
  SpacedBetween,
  UnstyledLink,
  VerticalAligned,
  Button,
} from '@rebrowse/elements';
import type { Theme } from 'baseui/theme';
import type { StyleObject } from 'styletron-react';

type Props = {
  helpBaseURL: string;
  theme: Theme;
  css: (arg: StyleObject) => string;
};

export const AccountsNavbar = ({ helpBaseURL, theme, css }: Props) => {
  return (
    <Block
      as="nav"
      padding={theme.sizing.scale600}
      className={css({ borderBottom: `1px solid ${theme.colors.primary200}` })}
    >
      <SpacedBetween>
        <VerticalAligned>
          <H6 margin={0}>Rebrowse</H6>
        </VerticalAligned>
        <Block>
          <UnstyledLink
            href={helpBaseURL}
            $style={{ marginRight: theme.sizing.scale600 }}
          >
            <Button
              size="compact"
              kind="minimal"
              // @ts-expect-error wrong typing
              tabIndex={-1}
            >
              Help
            </Button>
          </UnstyledLink>
        </Block>
      </SpacedBetween>
    </Block>
  );
};
