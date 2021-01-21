import React from 'react';
import { styled } from 'baseui';
import { Block, BlockProps } from 'baseui/block';
import { Paragraph1 } from 'baseui/typography';

import { VerticalAligned } from '../VerticalAligned';
import { Flex } from '../Flex';

const DividerLine = styled(Block, ({ $theme }) => ({
  flex: 1,
  height: '1px',
  marginTop: $theme.sizing.scale1000,
  marginBottom: $theme.sizing.scale1000,
  backgroundColor: $theme.colors.primary100,
}));

const DividerTextContainer = styled(VerticalAligned, ({ $theme }) => ({
  marginLeft: $theme.sizing.scale600,
  marginRight: $theme.sizing.scale600,
}));

const DividerText = styled(Paragraph1, {
  textTransform: 'uppercase',
  margin: 0,
});

const DividerOr = React.forwardRef<HTMLDivElement, BlockProps>(
  ({ children = 'Or', ...rest }, ref) => {
    return (
      <DividerTextContainer {...rest} ref={ref}>
        <DividerText>{children}</DividerText>
      </DividerTextContainer>
    );
  }
);

const DividerBase = styled(Flex, {});

export const Divider = Object.assign(DividerBase, {
  Line: DividerLine,
  Or: DividerOr,
});
