import React, { useState } from 'react';
import { Controller, FieldError, useForm } from 'react-hook-form';
import { Button } from 'atoms/Button';
import type { PhoneNumber } from '@rebrowse/types';
import type { Meta } from '@storybook/react';
import { Block } from 'baseui/block';
import { action } from '@storybook/addon-actions';
import { FormControl } from 'baseui/form-control';

import { PhoneNumberInput, Value } from './PhoneNumberInput';

export default {
  title: 'inputs/PhoneNumberInput',
  component: PhoneNumberInput,
} as Meta;

export const ControlledOptionalWithError = () => {
  const [phoneNumber, setPhoneNumber] = useState<Value>();

  return (
    <FormControl error="Something went wrong">
      <PhoneNumberInput
        value={phoneNumber}
        onChange={setPhoneNumber}
        onBlur={action('onBlur')}
        onSelectBlur={action('onSelectBlur')}
        error
      />
    </FormControl>
  );
};

export const UncontrolledRequired = () => {
  const { control, handleSubmit, setError, errors } = useForm<{
    phoneNumber: Partial<PhoneNumber>;
  }>();

  const onSubmit = handleSubmit((values) => {
    if (!values.phoneNumber?.digits) {
      setError('phoneNumber', { message: 'Required' });
      return;
    }

    if (values.phoneNumber.digits.charAt(0) === '+') {
      setError('phoneNumber', {
        message: 'Please enter a phone number without the country dial code',
      });
    }
  });

  return (
    <form onSubmit={onSubmit} noValidate>
      <FormControl
        label="Phone number"
        error={(errors.phoneNumber as FieldError)?.message}
      >
        <Controller
          name="phoneNumber"
          control={control}
          as={
            <PhoneNumberInput
              error={Boolean((errors.phoneNumber as FieldError)?.message)}
              required
            />
          }
        />
      </FormControl>

      <Block marginTop="16px">
        <Button type="submit">Submit</Button>
      </Block>
    </form>
  );
};
