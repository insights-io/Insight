import React from 'react';
import { FormControl } from 'baseui/form-control';
import { PinCode } from 'baseui/pin-code';

type Props = {
  code: string[];
  handleChange: (code: string[]) => void;
  error: React.ReactNode | undefined;
  disabled?: boolean;
};

const FormTfaInput = React.forwardRef<HTMLInputElement, Props>(
  ({ code, handleChange, error, disabled = false }, ref) => {
    return (
      <FormControl label="Google verification code" error={error}>
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

export default FormTfaInput;
