import React from 'react';
import { useStyletron } from 'baseui';
import { Block } from 'baseui/block';
import { H6 } from 'baseui/typography';
import { Button, SHAPE } from 'baseui/button';
import {
  SpacedBetween,
  UnstyledLink,
  VerticalAligned,
} from '@rebrowse/elements';

type Props = {
  appBaseURL: string;
  helpBaseURL: string;
};

export const Topbar = ({ appBaseURL, helpBaseURL }: Props) => {
  const [_css, theme] = useStyletron();

  return (
    <Block
      as="nav"
      padding={theme.sizing.scale600}
      $style={{ borderBottom: `1px solid ${theme.colors.primary200}` }}
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
            <Button shape={SHAPE.pill} size="compact" kind="minimal">
              Help
            </Button>
          </UnstyledLink>

          <UnstyledLink href={appBaseURL}>
            <Button shape={SHAPE.pill} size="compact" kind="minimal">
              Log in
            </Button>
          </UnstyledLink>
        </Block>
      </SpacedBetween>
    </Block>
  );
};
