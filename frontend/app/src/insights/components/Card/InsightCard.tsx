import { FlexColumn, FlexColumnProps } from '@rebrowse/elements';
import { styled } from 'baseui';
import { Block, BlockProps } from 'baseui/block';
import { H6, ParagraphXSmall } from 'baseui/typography';
import React from 'react';

export type CardProps = FlexColumnProps;

const Title = (props: BlockProps) => {
  return <H6 margin={0} as="p" color="white" {...props} />;
};

const Subtitle = (props: BlockProps) => {
  return <ParagraphXSmall margin={0} color="#6086d6" {...props} />;
};

const Content = styled(Block, ({ $theme }) => ({
  flex: 1,
  marginTop: $theme.sizing.scale700,
}));

const Footer = styled(Block, ({ $theme }) => ({
  display: 'flex',
  marginTop: $theme.sizing.scale700,
}));

export const Card = (props: CardProps) => {
  return (
    <FlexColumn
      backgroundColor="#27273f"
      padding="24px"
      $style={{ borderRadius: '8px' }}
      {...props}
    />
  );
};

Card.Title = Title;
Card.Subtitle = Subtitle;
Card.Content = Content;
Card.Footer = Footer;
