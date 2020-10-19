import React, { useState } from 'react';
import { Card, CardOverrides } from 'baseui/card';
import { useForm } from 'react-hook-form';
import { FormControl } from 'baseui/form-control';
import { PASSWORD_VALIDATION } from 'modules/auth/validation/password';
import type {
  ChangePasswordDTO,
  APIError,
  APIErrorDataResponse,
} from '@insight/types';
import { AuthApi } from 'api/auth';
import FormError from 'shared/components/FormError';
import { toaster } from 'baseui/toast';
import { Input, Button } from '@insight/elements';

type Props = {
  overrides?: CardOverrides;
};

export const ChangePassword = ({ overrides }: Props) => {
  const { register, handleSubmit, errors, watch, setError } = useForm<
    ChangePasswordDTO
  >();
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [formError, setFormError] = useState<APIError | undefined>();

  const onSubmit = handleSubmit((formData) => {
    if (isSubmitting) {
      return;
    }
    setIsSubmitting(true);

    AuthApi.password
      .change(formData)
      .then(() => {
        toaster.positive('Password changed', {});
        setFormError(undefined);
      })
      .catch(async (error) => {
        const errorResponse: APIErrorDataResponse = await error.response.json();
        const { errors: apiErrors } = errorResponse.error;

        console.log({ apiErrors });

        if (apiErrors) {
          Object.keys(apiErrors).forEach((field) => {
            setError(field as keyof ChangePasswordDTO, {
              message: apiErrors[field],
            });
          });
        } else {
          setFormError(errorResponse.error);
        }
      })
      .finally(() => setIsSubmitting(false));
  });

  return (
    <Card title="Change password" overrides={overrides}>
      <form onSubmit={onSubmit} noValidate>
        <FormControl error={errors.currentPassword?.message}>
          <Input
            placeholder="Current password"
            name="currentPassword"
            type="password"
            autoComplete="current-password"
            ref={register}
            inputRef={register(PASSWORD_VALIDATION)}
            error={Boolean(errors.currentPassword)}
          />
        </FormControl>

        <FormControl error={errors.newPassword?.message}>
          <Input
            placeholder="New password"
            name="newPassword"
            type="password"
            autoComplete="new-password"
            ref={register}
            inputRef={register({
              ...PASSWORD_VALIDATION,
              validate: (value) => {
                return value === watch('currentPassword')
                  ? 'New password cannot be the same as the previous one!'
                  : true;
              },
            })}
            error={Boolean(errors.newPassword)}
          />
        </FormControl>

        <FormControl error={errors.confirmNewPassword?.message}>
          <Input
            placeholder="Confirm new password"
            name="confirmNewPassword"
            type="password"
            autoComplete="new-password"
            ref={register}
            inputRef={register({
              ...PASSWORD_VALIDATION,
              validate: (value) => {
                return value !== watch('newPassword')
                  ? 'Passwords must match!'
                  : true;
              },
            })}
            error={Boolean(errors.confirmNewPassword)}
          />
        </FormControl>

        <Button
          type="submit"
          isLoading={isSubmitting}
          $style={{ width: '100%' }}
        >
          Save new password
        </Button>
        {formError && <FormError error={formError} />}
      </form>
    </Card>
  );
};
