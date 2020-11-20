import React from 'react';
import { useStyletron } from 'baseui';
import { Block } from 'baseui/block';
import { H1, H2 } from 'baseui/typography';
import { FlexColumn, VerticalAligned } from '@rebrowse/elements';

type Props = {
  children: React.ReactNode;
  subtitle?: string;
};

export const AuthPageLayout = ({ children, subtitle }: Props) => {
  const [_css, theme] = useStyletron();
  return (
    <FlexColumn height="100%">
      <VerticalAligned
        height="100%"
        width="100%"
        maxWidth="720px"
        marginLeft="auto"
        marginRight="auto"
        padding={theme.sizing.scale600}
      >
        <Block height="100%">
          <Block
            marginBottom={theme.sizing.scale700}
            $style={{ textAlign: 'center' }}
          >
            <H1
              marginBottom={theme.sizing.scale400}
              $style={{ fontWeight: 700, fontSize: '24px' }}
            >
              Rebrowse
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
      </VerticalAligned>
    </FlexColumn>
  );
};
