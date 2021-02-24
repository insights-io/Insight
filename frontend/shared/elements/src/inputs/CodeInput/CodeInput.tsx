import React, { forwardRef } from 'react';
import { FormControl } from 'baseui/form-control';
import { PinCode } from 'baseui/pin-code';
import { Block } from 'baseui/block';

import { inputBorderRadius } from '../Input';

export type Props = {
  code: string[];
  handleChange: (code: string[]) => void;
  error?: React.ReactNode | undefined;
  label?: string;
  disabled?: boolean;
  inputRef?: React.RefObject<HTMLInputElement>;
};

export const CodeInput = forwardRef<HTMLDivElement, Props>(
  ({ code, handleChange, label, error, disabled = false, inputRef }, ref) => {
    return (
      <Block ref={ref}>
        <FormControl
          label={label}
          error={error}
          overrides={{ ControlContainer: { style: { marginBottom: 0 } } }}
        >
          <PinCode
            inputRef={inputRef}
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
      </Block>
    );
  }
);
