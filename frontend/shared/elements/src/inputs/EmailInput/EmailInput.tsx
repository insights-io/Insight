import React, { forwardRef } from 'react';

import { Input, Props as InputProps } from '../Input';

export type EmailInputProps = InputProps;

export const EmailInput = forwardRef<HTMLInputElement, EmailInputProps>(
  (props, ref) => {
    return (
      <Input
        ref={ref}
        id="email"
        type="email"
        name="email"
        placeholder="Email"
        {...props}
      />
    );
  }
);
