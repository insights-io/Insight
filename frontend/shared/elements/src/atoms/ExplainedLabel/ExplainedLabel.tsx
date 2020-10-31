import React from 'react';
import { ParagraphXSmall } from 'baseui/typography';
import type { BlockProps } from 'baseui/block';

import { FlexColumn } from '../FlexColumn';
import { Label } from '../Label';

type Props = BlockProps & {
  children: React.ReactNode;
  explanation: string;
  for?: string;
};

export const ExplainedLabel = ({
  children,
  explanation,
  for: labelFor,
  ...rest
}: Props) => {
  return (
    <FlexColumn
      as="label"
      overrides={{ Block: { props: { for: labelFor } } }}
      {...rest}
    >
      <Label as="div">{children}</Label>
      <ParagraphXSmall margin={0}>{explanation}</ParagraphXSmall>
    </FlexColumn>
  );
};
