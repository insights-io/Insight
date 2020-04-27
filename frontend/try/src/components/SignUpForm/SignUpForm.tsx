import React, { useState } from 'react';
import { useStyletron } from 'baseui';
import { Block } from 'baseui/block';
import { Input, InputOverrides } from 'baseui/input';
import { FormControl } from 'baseui/form-control';
import { Button } from 'baseui/button';
import {
  PhoneInputNext,
  CountrySelectDropdown,
  Country,
  COUNTRIES,
} from 'baseui/phone-input';
import { useForm, Controller } from 'react-hook-form';

export type FormData = {
  firstName: string;
  lastName: string;
  company: string;
  phoneNumber?: string;
  email: string;
  password: string;
};

export type Props = {
  onSubmit: <T>(data: FormData) => Promise<T>;
  minPasswordLength?: number;
};

const SignUpForm = ({
  onSubmit: onSubmitProp,
  minPasswordLength = 8,
}: Props) => {
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [country, setCountry] = useState<Country>(COUNTRIES.US);
  const [css, theme] = useStyletron();
  const { register, handleSubmit, errors, control } = useForm<FormData>();

  const onSubmit = handleSubmit(({ phoneNumber, ...rest }) => {
    if (isSubmitting) {
      return;
    }
    setIsSubmitting(true);

    const formData = phoneNumber
      ? { ...rest, phoneNumber: `${country.dialCode}${phoneNumber}` }
      : rest;

    onSubmitProp(formData).finally(() => {
      setIsSubmitting(false);
    });
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
      <Block display="flex">
        <Block width="100%" marginRight={theme.sizing.scale600}>
          <FormControl label="First name" error={errors.firstName?.message}>
            <Input
              overrides={inputOverrides}
              name="firstName"
              placeholder="First name"
              required
              inputRef={register({ required: 'Required' })}
              error={Boolean(errors.firstName)}
            />
          </FormControl>
        </Block>
        <Block width="100%" marginLeft={theme.sizing.scale600}>
          <FormControl label="Last Name" error={errors.lastName?.message}>
            <Input
              overrides={inputOverrides}
              name="lastName"
              placeholder="Last name"
              required
              inputRef={register({ required: 'Required' })}
              error={Boolean(errors.lastName)}
            />
          </FormControl>
        </Block>
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

      <Block>
        <FormControl
          error={errors.phoneNumber?.message}
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
            control={control}
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
                error={Boolean(errors.phoneNumber)}
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
    </form>
  );
};

export default SignUpForm;
