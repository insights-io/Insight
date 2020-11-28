import React, { forwardRef } from 'react';

import { Input, Props as InputProps } from '../Input';

export type PasswordInputProps = InputProps;

export const PasswordInput = forwardRef<HTMLInputElement, PasswordInputProps>(
  (props, ref) => {
    return (
      <Input
        ref={ref}
        id="password"
        type="password"
        name="password"
        placeholder="Password"
        {...props}
      />
    );
  }
);
