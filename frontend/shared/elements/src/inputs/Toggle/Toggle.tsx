import React, { forwardRef } from 'react';
import { Checkbox, CheckboxProps, STYLE_TYPE } from 'baseui/checkbox';

type Props = Omit<CheckboxProps, 'checkmarkType'> & {
  id?: string;
  name?: string;
};

export const Toggle = forwardRef<HTMLInputElement, Props>(
  ({ id, name, ...rest }, ref) => {
    return (
      <Checkbox
        checkmarkType={STYLE_TYPE.toggle_round}
        inputRef={ref}
        overrides={{ Input: { props: { id, name } } }}
        {...rest}
      />
    );
  }
);
