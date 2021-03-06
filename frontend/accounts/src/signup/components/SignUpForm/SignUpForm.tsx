import React, { useState } from 'react';
import { useStyletron } from 'baseui';
import { Block } from 'baseui/block';
import { FormControl } from 'baseui/form-control';
import { Controller, FieldError, useForm } from 'react-hook-form';
import {
  APIErrorDataResponse,
  APIError,
  SignUpRequestDTO,
} from '@rebrowse/types';
import { FormError } from 'shared/components/FormError';
import { useRouter } from 'next/router';
import {
  PhoneNumberInput,
  Button,
  Input,
  Label,
  PasswordInput,
  EmailInput,
} from '@rebrowse/elements';

export type SignUpFormValues = Omit<SignUpRequestDTO, 'redirect'>;

export type Props = {
  onSubmit: (data: SignUpFormValues) => Promise<unknown>;
  minPasswordLength?: number;
  email?: string;
  redirect: string;
};

export const SignUpForm = ({
  onSubmit: onSubmitProp,
  minPasswordLength = 8,
  ...defaultValues
}: Props) => {
  const router = useRouter();
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [formError, setFormError] = useState<APIError | undefined>();
  const [_css, theme] = useStyletron();
  const { register, handleSubmit, errors, control } = useForm<SignUpFormValues>(
    { defaultValues }
  );

  const onSubmit = handleSubmit((values) => {
    if (isSubmitting) {
      return;
    }

    setIsSubmitting(true);
    setFormError(undefined);

    onSubmitProp(values)
      .then(() => router.push('/signup-confirm'))
      .catch(async (error) => {
        const errorDTO: APIErrorDataResponse = await error.response.json();
        setFormError(errorDTO.error);
      })
      .finally(() => setIsSubmitting(false));
  });

  return (
    <form onSubmit={onSubmit} noValidate>
      <FormControl
        htmlFor="fullName"
        label={
          <Label required as="span">
            Full name
          </Label>
        }
        error={errors.fullName?.message}
      >
        <Input
          name="fullName"
          placeholder="John Doe"
          required
          ref={(ref) => {
            register(ref, { required: 'Required' });
            ref?.focus();
          }}
          error={Boolean(errors.fullName)}
        />
      </FormControl>

      <FormControl
        htmlFor="company"
        label={
          <Label required as="span">
            Company
          </Label>
        }
        error={errors.company?.message}
      >
        <Input
          placeholder="Example"
          name="company"
          ref={register({ required: 'Required' })}
          error={Boolean(errors.company)}
        />
      </FormControl>

      <FormControl
        htmlFor="phoneNumber"
        label={<Label as="span">Phone number</Label>}
        error={(errors.phoneNumber as FieldError)?.message}
      >
        <Controller
          as={PhoneNumberInput}
          name="phoneNumber"
          control={control}
          error={Boolean(errors.phoneNumber)}
          placeholder="51111222"
        />
      </FormControl>

      <FormControl
        htmlFor="email"
        label={
          <Label required as="span">
            Email
          </Label>
        }
        error={errors.email?.message}
      >
        <EmailInput
          placeholder="john.doe@gmail.com"
          required
          ref={register({
            required: 'Required',
            pattern: {
              value: /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,4}$/i,
              message: 'Invalid email address',
            },
          })}
          error={Boolean(errors.email)}
        />
      </FormControl>

      <FormControl
        label={
          <Label required as="span">
            Password
          </Label>
        }
        htmlFor="password"
        error={errors.password?.message}
      >
        <PasswordInput
          ref={register({
            required: 'Required',
            minLength: {
              value: minPasswordLength,
              message: `Password must be at least ${minPasswordLength} characters long`,
            },
          })}
          error={Boolean(errors.password)}
        />
      </FormControl>

      <Block marginTop={theme.sizing.scale1200}>
        <Button
          type="submit"
          $style={{ width: '100%' }}
          isLoading={isSubmitting}
          disabled={isSubmitting}
        >
          Get started
        </Button>
      </Block>

      {formError && <FormError error={formError} />}
    </form>
  );
};
