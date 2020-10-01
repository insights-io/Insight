import { Flex, FlexColumn, VerticalAligned } from '@insight/elements';
import { useStyletron } from 'baseui';
import { Avatar } from 'baseui/avatar';
import { Block } from 'baseui/block';
import React, { forwardRef } from 'react';
import { StyleObject } from 'styletron-react';

export type Props = {
  title: string;
  titleExtra?: JSX.Element;
  subtitle: string;
  expanded?: boolean;
  overrides?: {
    Avatar?: {
      style: StyleObject;
    };
    Root: {
      style?: StyleObject;
    };
    Subtitle?: {
      style?: StyleObject;
    };
  };
};

export const BannerCard = forwardRef<HTMLDivElement, Props>(
  ({ title, titleExtra, subtitle, overrides, expanded = true }, ref) => {
    const [_css, theme] = useStyletron();
    return (
      <Flex $style={overrides?.Root?.style} className="banner--card" ref={ref}>
        <Avatar
          name={title}
          overrides={{
            Root: {
              style: {
                ...overrides?.Avatar?.style,
                backgroundColor: theme.colors.accent600,
              },
            },
          }}
        />
        {expanded && (
          <VerticalAligned marginLeft={theme.sizing.scale400}>
            <FlexColumn>
              <Flex $style={{ fontWeight: 500, fontSize: '1.1rem' }}>
                {title}
                {titleExtra}
              </Flex>
              <Block
                $style={{
                  fontSize: '0.8rem',
                  color: '#d3d3d3',
                  ...overrides?.Subtitle?.style,
                }}
              >
                {subtitle}
              </Block>
            </FlexColumn>
          </VerticalAligned>
        )}
      </Flex>
    );
  }
);
