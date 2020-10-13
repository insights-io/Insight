import React from 'react';
import { Block } from 'baseui/block';
import { FormControl } from 'baseui/form-control';
import { useStyletron } from 'baseui';
import { Controller, Control, FieldError, FieldValues } from 'react-hook-form';
import {
  PhoneInputNext,
  CountrySelectDropdown,
  Country,
} from 'baseui/phone-input';

import { Input, inputBorderRadius } from '../Input';

export type Values = {
  phoneNumber: string | undefined;
};

export type Props<V> = {
  country: Country;
  setCountry: (country: Country) => void;
  error: FieldError | undefined;
  control: Control<V>;
};

export const PhoneNumberInput = <TFieldValues extends Values = Values>({
  country,
  setCountry,
  error,
  control,
}: Props<TFieldValues>) => {
  const [_css, theme] = useStyletron();
  return (
    <Block>
      <FormControl
        error={error?.message}
        label={() => (
          <Block>
            Phone{' '}
            <Block as="span" color={theme.colors.primary400}>
              (optional)
            </Block>
          </Block>
        )}
      >
        <Controller
          control={control as Control<FieldValues>}
          name="phoneNumber"
          defaultValue=""
          rules={{
            pattern: {
              value: /^(?![+]).*$/,
              message:
                'Please enter a phone number without the country dial code.',
            },
          }}
          as={
            <PhoneInputNext
              placeholder="Phone number"
              country={country}
              onCountryChange={({ option }) => setCountry(option as Country)}
              error={Boolean(error)}
              overrides={{
                Input: { component: Input },
                CountrySelect: {
                  props: {
                    overrides: {
                      Dropdown: { component: CountrySelectDropdown },
                      ControlContainer: { style: inputBorderRadius },
                    },
                  },
                },
              }}
            />
          }
        />
      </FormControl>
    </Block>
  );
};
