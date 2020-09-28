import React from 'react';
import { useStyletron } from 'baseui';
import { Block } from 'baseui/block';
import { H6 } from 'baseui/typography';
import { Button, SHAPE } from 'baseui/button';
import { SpacedBetween } from '@insight/elements';

type Props = {
  appBaseURL: string;
  helpBaseURL: string;
};

const Topbar = ({ appBaseURL, helpBaseURL }: Props) => {
  const [css, theme] = useStyletron();

  return (
    <Block
      as="nav"
      padding={theme.sizing.scale600}
      $style={{ borderBottom: `1px solid ${theme.colors.primary200}` }}
    >
      <SpacedBetween>
        <H6 margin={0}>Insight</H6>
        <Block>
          <a
            href={helpBaseURL}
            className={css({
              marginRight: theme.sizing.scale600,
              textDecoration: 'none',
            })}
          >
            <Button shape={SHAPE.pill} size="compact" kind="minimal">
              Help
            </Button>
          </a>

          <a href={appBaseURL} className={css({ textDecoration: 'none' })}>
            <Button shape={SHAPE.pill} size="compact" kind="minimal">
              Log in
            </Button>
          </a>
        </Block>
      </SpacedBetween>
    </Block>
  );
};

export default Topbar;
