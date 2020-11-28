import { BlockProps } from 'baseui/block';
import React, { forwardRef } from 'react';
import { Label4 } from 'baseui/typography';

type Props = BlockProps & {
  required?: boolean;
  for?: string;
};

export const LABEL_REQUIRED_STYLE = {
  marginLeft: '4px',
  content: `'*'`,
  color: 'red',
};

export const Label = forwardRef<HTMLDivElement, Props>(
  ({ $style, required, ...rest }, ref) => {
    return (
      <Label4
        ref={ref}
        as="label"
        $style={{
          ...$style,
          ':after': required ? LABEL_REQUIRED_STYLE : undefined,
        }}
        {...rest}
      />
    );
  }
);
