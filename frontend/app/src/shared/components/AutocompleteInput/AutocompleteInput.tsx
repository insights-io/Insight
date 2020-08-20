import React from 'react';
import { Select, SelectProps, STATE_CHANGE_TYPE } from 'baseui/select';

type Props = Omit<
  SelectProps,
  'onInputChange' | 'onChange' | 'value' | 'valueKey' | 'options'
> & {
  onChange: (value: string) => void;
  value: string;
  options: string[];
};

const AutocompleteInput = ({ onChange, value, options, ...rest }: Props) => {
  return (
    <Select
      valueKey="label"
      onChange={(params) => {
        const nextValue = params.option ? (params.option.label as string) : '';
        if (params.type === STATE_CHANGE_TYPE.remove && nextValue) {
          onChange(nextValue.substring(0, nextValue.length - 1));
        } else {
          onChange(nextValue);
        }
      }}
      onInputChange={(event) => onChange(event.currentTarget.value)}
      value={value === '' ? [] : [{ label: value }]}
      options={options.map((label) => ({ label }))}
      {...rest}
    />
  );
};

export default AutocompleteInput;
