import React, { useState } from 'react';
import { useStyletron } from 'baseui';
import { Block } from 'baseui/block';
import { Input, InputOverrides } from 'baseui/input';
import { FormControl } from 'baseui/form-control';
import { Button } from 'baseui/button';
import { Country, COUNTRIES } from 'baseui/phone-input';
import { useForm } from 'react-hook-form';
import {
  APIErrorDataResponse,
  APIError,
  SignUpRequestDTO,
} from '@insight/types';
import FormError from 'shared/components/FormError';
import Router from 'next/router';
import { PhoneNumberInput } from '@insight/elements';

type SignUpFormData = Omit<SignUpRequestDTO, 'phoneNumber'> & {
  phoneNumber: string | undefined;
};

export type Props = {
  onSubmit: (data: SignUpRequestDTO) => Promise<unknown>;
  minPasswordLength?: number;
};

export const SignUpForm = ({
  onSubmit: onSubmitProp,
  minPasswordLength = 8,
}: Props) => {
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [formError, setFormError] = useState<APIError | undefined>();
  const [country, setCountry] = useState<Country>(COUNTRIES.US);
  const [_css, theme] = useStyletron();
  const { register, handleSubmit, errors, control } = useForm<SignUpFormData>();

  const onSubmit = handleSubmit(({ phoneNumber, ...rest }) => {
    if (isSubmitting) {
      return;
    }
    setIsSubmitting(true);
    setFormError(undefined);

    const signUpFormData = phoneNumber
      ? {
          ...rest,
          phoneNumber: { countryCode: country.dialCode, digits: phoneNumber },
        }
      : rest;

    onSubmitProp(signUpFormData)
      .then(() => Router.push('/signup-confirm'))
      .catch(async (error) => {
        const errorDTO: APIErrorDataResponse = await error.response.json();
        setFormError(errorDTO.error);
      })
      .finally(() => setIsSubmitting(false));
  });

  const inputBorderRadius = {
    borderBottomRightRadius: theme.sizing.scale100,
    borderTopRightRadius: theme.sizing.scale100,
    borderTopLeftRadius: theme.sizing.scale100,
    borderBottomLeftRadius: theme.sizing.scale100,
  };

  const inputOverrides: InputOverrides = {
    InputContainer: { style: inputBorderRadius },
    Input: { style: inputBorderRadius },
  };

  return (
    <form onSubmit={onSubmit} noValidate>
      <Block>
        <FormControl label="Full name" error={errors.fullName?.message}>
          <Input
            overrides={inputOverrides}
            name="fullName"
            placeholder="Full name"
            required
            inputRef={register({ required: 'Required' })}
            error={Boolean(errors.fullName)}
          />
        </FormControl>
      </Block>

      <Block>
        <FormControl label="Company" error={errors.company?.message}>
          <Input
            overrides={inputOverrides}
            placeholder="Company"
            name="company"
            inputRef={register({ required: 'Required' })}
            error={Boolean(errors.company)}
          />
        </FormControl>
      </Block>

      <PhoneNumberInput
        control={control}
        error={errors.phoneNumber}
        country={country}
        setCountry={setCountry}
      />

      <Block>
        <FormControl label="Email" error={errors.email?.message}>
          <Input
            overrides={inputOverrides}
            name="email"
            type="email"
            placeholder="Email"
            required
            inputRef={register({
              required: 'Required',
              pattern: {
                value: /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,4}$/i,
                message: 'Invalid email address',
              },
            })}
            error={Boolean(errors.email)}
          />
        </FormControl>
      </Block>

      <Block marginBottom={theme.sizing.scale1200}>
        <FormControl label="Password" error={errors.password?.message}>
          <Input
            overrides={inputOverrides}
            placeholder="Password"
            name="password"
            type="password"
            ref={register}
            inputRef={register({
              required: 'Required',
              minLength: {
                value: minPasswordLength,
                message: `Password must be at least ${minPasswordLength} characters long`,
              },
            })}
            error={Boolean(errors.password)}
          />
        </FormControl>
      </Block>

      <Button type="submit" $style={{ width: '100%' }} isLoading={isSubmitting}>
        Get started
      </Button>

      {formError && <FormError error={formError} />}
    </form>
  );
};
