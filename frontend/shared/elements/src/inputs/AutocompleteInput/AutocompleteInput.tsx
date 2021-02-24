import React, { forwardRef } from 'react';
import { Select, SelectProps, STATE_CHANGE_TYPE } from 'baseui/select';

import { inputBorderRadius } from '../Input';

type Option = {
  label: string;
  value: string;
};

export type Props = Omit<
  SelectProps,
  'onInputChange' | 'onChange' | 'value' | 'valueKey' | 'options'
> & {
  onChange: (value: string) => void;
  options: (string | Option)[];
  value: string;
};

export const AutocompleteInput = forwardRef<Select, Props>(
  ({ onChange, value, options, ...rest }, ref) => {
    return (
      <Select
        ref={ref}
        valueKey="value"
        labelKey="label"
        onChange={(params) => {
          const nextValue = (params.option as Option | null)?.value || '';
          if (params.type === STATE_CHANGE_TYPE.remove && nextValue) {
            onChange(nextValue.substring(0, nextValue.length - 1));
          } else {
            onChange(nextValue);
          }
        }}
        onInputChange={(event) => onChange(event.currentTarget.value)}
        value={value === '' ? [] : [{ value, label: value }]}
        options={options.map((option) =>
          typeof option === 'string' ? { value: option, label: option } : option
        )}
        overrides={{ ControlContainer: { style: inputBorderRadius } }}
        {...rest}
      />
    );
  }
);

export default AutocompleteInput;
