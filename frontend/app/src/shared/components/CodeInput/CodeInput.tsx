import React from 'react';
import { FormControl } from 'baseui/form-control';
import { PinCode } from 'baseui/pin-code';

type Props = {
  code: string[];
  handleChange: (code: string[]) => void;
  error: React.ReactNode | undefined;
  label: string;
  disabled?: boolean;
};

const CodeInput = React.forwardRef<HTMLInputElement, Props>(
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
        />
      </FormControl>
    );
  }
);

export default CodeInput;
