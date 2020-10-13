import React, { forwardRef } from 'react';
import { Checkbox, CheckboxProps, STYLE_TYPE } from 'baseui/checkbox';

type Props = Omit<CheckboxProps, 'checkmarkType'>;

export const Toggle = forwardRef<HTMLInputElement, Props>((props, ref) => {
  return (
    <Checkbox
      checkmarkType={STYLE_TYPE.toggle_round}
      inputRef={ref}
      {...props}
    />
  );
});
