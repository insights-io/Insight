import React from 'react';
import Head from 'next/head';
import { useStyletron } from 'baseui';
import { H5, H6, Paragraph3 } from 'baseui/typography';
import { Block } from 'baseui/block';
import { Button, SHAPE } from 'baseui/button';
import SignUpForm from 'components/SignUpForm';
import config from 'shared/config';

const GetStarted = () => {
  const [css, theme] = useStyletron();

  return (
    <Block display="flex" flexDirection="column" height="100%">
      <Head>
        <title>Insight | Sign up</title>
      </Head>
      <nav
        className={css({
          padding: theme.sizing.scale600,
          borderBottom: `1px solid ${theme.colors.primary200}`,
        })}
      >
        <Block display="flex" justifyContent="space-between">
          <H6 margin={0}>Insight</H6>
          <Block>
            <a
              href={config.helpBaseURL}
              className={css({
                marginRight: theme.sizing.scale600,
                textDecoration: 'none',
              })}
            >
              <Button shape={SHAPE.pill} size="compact" kind="minimal">
                Help
              </Button>
            </a>

            <a
              href={config.appBaseURL}
              className={css({ textDecoration: 'none' })}
            >
              <Button shape={SHAPE.pill} size="compact" kind="minimal">
                Log in
              </Button>
            </a>
          </Block>
        </Block>
      </nav>
      <Block height="100%" padding={theme.sizing.scale600}>
        <Block
          width="100%"
          maxWidth="720px"
          marginLeft="auto"
          marginRight="auto"
        >
          <Block
            marginBottom={theme.sizing.scale700}
            $style={{ textAlign: 'center' }}
          >
            <H5
              marginBottom={theme.sizing.scale400}
              $style={{ fontWeight: 700 }}
            >
              Start your free trial now.
            </H5>

            <Paragraph3
              marginTop={theme.sizing.scale400}
              color={theme.colors.primary400}
            >
              You&apos;re minutes away from insights.
            </Paragraph3>
          </Block>

          <SignUpForm
            onSubmit={() => new Promise((resolve) => setTimeout(resolve, 200))}
          />
        </Block>
      </Block>
    </Block>
  );
};

export default GetStarted;
