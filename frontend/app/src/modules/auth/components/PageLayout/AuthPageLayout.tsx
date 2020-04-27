import React from 'react';
import { useStyletron } from 'baseui';
import { Block } from 'baseui/block';
import { H1, H2 } from 'baseui/typography';

type Props = {
  children: React.ReactNode;
  subtitle?: string;
};

const AuthPageLayout = ({ children, subtitle }: Props) => {
  const [_css, theme] = useStyletron();
  return (
    <Block display="flex" flexDirection="column" height="100%">
      <Block
        height="100%"
        width="100%"
        maxWidth="720px"
        marginLeft="auto"
        marginRight="auto"
        display="flex"
        flexDirection="column"
        justifyContent="center"
        padding={theme.sizing.scale600}
      >
        <Block
          marginBottom={theme.sizing.scale700}
          $style={{ textAlign: 'center' }}
        >
          <H1
            marginBottom={theme.sizing.scale400}
            $style={{ fontWeight: 700, fontSize: '24px' }}
          >
            Insight
          </H1>

          {subtitle && (
            <H2
              marginTop={theme.sizing.scale400}
              $style={{ fontWeight: 700, fontSize: '18px' }}
            >
              {subtitle}
            </H2>
          )}
        </Block>

        {children}
      </Block>
    </Block>
  );
};

export default AuthPageLayout;
