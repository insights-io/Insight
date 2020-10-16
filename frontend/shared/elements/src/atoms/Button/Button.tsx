import React, { forwardRef } from 'react';
import { Button as BaseUiButton, ButtonProps } from 'baseui/button';

import { expandBorderRadius } from '../../helpers';

export const buttonBorderRadius = expandBorderRadius('8px');

export type Props = ButtonProps;

export const Button = forwardRef<HTMLButtonElement, Props>(
  ({ $style, ...rest }, ref) => {
    return (
      <BaseUiButton
        ref={ref}
        $style={{ ...buttonBorderRadius, ...$style }}
        {...rest}
      />
    );
  }
);
