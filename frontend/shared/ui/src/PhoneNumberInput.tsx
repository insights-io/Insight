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

type Props<T> = {
  country: Country;
  setCountry: (country: Country) => void;
  error: FieldError | undefined;
  control: Control<T>;
};

function PhoneNumberInput<T extends FieldValues>({
  country,
  setCountry,
  error,
  control,
}: Props<T>) {
  const [css, theme] = useStyletron();
  return (
    <Block>
      <FormControl
        error={error?.message}
        label={() => (
          <div>
            Phone{' '}
            <span className={css({ color: theme.colors.primary400 })}>
              (optional)
            </span>
          </div>
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
                CountrySelect: {
                  props: {
                    overrides: {
                      Dropdown: { component: CountrySelectDropdown },
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
}

export default PhoneNumberInput;
