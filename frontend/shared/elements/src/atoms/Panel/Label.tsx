import React from 'react';
import { ParagraphXSmall } from 'baseui/typography';
import type { BlockProps } from 'baseui/block';

import { FlexColumn } from '../FlexColumn';
import { Label as LabelBase } from '../Label';

type Props = BlockProps & {
  children: React.ReactNode;
  explanation?: string;
  for?: string;
};

export const Label = ({
  children,
  explanation,
  for: htmlFor,
  ...rest
}: Props) => {
  return (
    <FlexColumn
      as="label"
      overrides={{ Block: { props: { htmlFor } } }}
      {...rest}
    >
      <LabelBase as="div">{children}</LabelBase>
      {explanation && (
        <ParagraphXSmall margin={0}>{explanation}</ParagraphXSmall>
      )}
    </FlexColumn>
  );
};
