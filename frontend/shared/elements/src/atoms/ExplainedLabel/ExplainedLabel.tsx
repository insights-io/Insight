import React from 'react';
import { ParagraphXSmall } from 'baseui/typography';

import { VerticalAligned } from '../VerticalAligned';
import { Label } from '../Label';

type Props = {
  children: React.ReactNode;
  explanation: string;
  for?: string;
  width?: string;
};

export const ExplainedLabel = ({
  children,
  explanation,
  for: labelFor,
  ...rest
}: Props) => {
  return (
    <VerticalAligned
      as="label"
      overrides={{ Block: { props: { for: labelFor } } }}
      {...rest}
    >
      <Label as="div">{children}</Label>
      <ParagraphXSmall margin={0}>{explanation}</ParagraphXSmall>
    </VerticalAligned>
  );
};
