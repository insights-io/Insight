import React, { useMemo } from 'react';
import {
  PhoneInputNext,
  CountrySelectDropdown,
  Country,
  COUNTRIES,
} from 'baseui/phone-input';
import type { PhoneNumber } from '@rebrowse/types';
import type { InputProps } from 'baseui/input';

import { Input, inputBorderRadius } from '../Input';

import { getCountryFromCountryCode } from './utils';

export type Value = PhoneNumber;

export type Props = Pick<
  InputProps,
  | 'placeholder'
  | 'endEnhancer'
  | 'required'
  | 'clearable'
  | 'size'
  | 'onBlur'
  | 'disabled'
> & {
  error?: boolean;
  value?: Value;
  onChange?: (value: Value) => void;
  onSelectBlur?: (event: React.MouseEvent<HTMLInputElement>) => void;
};

const DEFAULT_VALUE: PhoneNumber = {
  digits: '',
  countryCode: COUNTRIES.US.dialCode,
};

export const PhoneNumberInput = ({
  required,
  value: { digits, countryCode } = DEFAULT_VALUE,
  onChange,
  onSelectBlur,
  ...rest
}: Props) => {
  const country = useMemo(() => getCountryFromCountryCode(countryCode), [
    countryCode,
  ]);

  return (
    <PhoneInputNext
      {...rest}
      required={required}
      country={country}
      onCountryChange={
        onChange
          ? ({ option }) => {
              const nextCountry = option as Country;
              onChange({ countryCode: nextCountry.dialCode, digits });
            }
          : undefined
      }
      overrides={{
        Input: {
          component: Input,
          props: {
            required,
            type: 'number',
            value: digits,
            step: 1,
            onKeyPress: (event) => {
              if (event.key === '+') {
                event.preventDefault();
              }
            },
            onChange: onChange
              ? (event) => {
                  const { value } = event.currentTarget;
                  onChange({ digits: value, countryCode });
                }
              : undefined,
          } as InputProps,
        },
        CountrySelect: {
          props: {
            onBlur: onSelectBlur,
            overrides: {
              Dropdown: { component: CountrySelectDropdown },
              ControlContainer: { style: inputBorderRadius },
            },
          },
        },
      }}
    />
  );
};
