import React, { forwardRef } from 'react';
import { FormControl } from 'baseui/form-control';
import { PinCode } from 'baseui/pin-code';

import { inputBorderRadius } from '../Input';

export type Props = {
  code: string[];
  handleChange: (code: string[]) => void;
  error: React.ReactNode | undefined;
  label: string;
  disabled?: boolean;
};

export const CodeInput = forwardRef<HTMLInputElement, Props>(
  ({ code, handleChange, label, error, disabled = false }, ref) => {
    return (
      <FormControl
        label={label}
        error={error}
        overrides={{ ControlContainer: { style: { marginBottom: 0 } } }}
      >
        <PinCode
          inputRef={ref}
          autoFocus
          disabled={disabled}
          error={error !== undefined}
          onChange={(data) => handleChange(data.values)}
          values={code}
          overrides={{
            Input: {
              props: {
                overrides: {
                  Root: { style: inputBorderRadius },
                },
              },
            },
          }}
        />
      </FormControl>
    );
  }
);
