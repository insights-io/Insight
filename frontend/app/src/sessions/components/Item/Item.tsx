import React from 'react';
import { Flex, FlexProps, VerticalAligned } from '@rebrowse/elements';
import { Paragraph3, Paragraph4 } from 'baseui/typography';
import { styled } from 'baseui';

export type ItemProps = FlexProps;

export const Item = (props: ItemProps) => <Flex {...props} />;

const Title = styled(Paragraph3, { margin: 0, fontWeight: 500 });
const Subtitle = styled(Paragraph4, { margin: 0 });
const Content = styled(VerticalAligned, ({ $theme }) => ({
  paddingLeft: $theme.sizing.scale400,
}));

Item.Content = Content;
Item.Title = Title;
Item.Subtitle = Subtitle;
