import React, { useState } from 'react';
import { useForm } from 'react-hook-form';
import { FormControl } from 'baseui/form-control';
import { PASSWORD_VALIDATION } from 'shared/constants/form-validation';
import type {
  ChangePasswordDTO,
  APIError,
  APIErrorDataResponse,
} from '@rebrowse/types';
import { FormError } from 'shared/components/FormError';
import { toaster } from 'baseui/toast';
import { Button, PasswordInput, Panel } from '@rebrowse/elements';
import { SIZE } from 'baseui/input';
import { applyApiFormErrors } from 'shared/utils/form';
import { client } from 'sdk';

export const ChangePassword = () => {
  const {
    register,
    handleSubmit,
    errors,
    watch,
    setError,
  } = useForm<ChangePasswordDTO>();
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [formError, setFormError] = useState<APIError | undefined>();

  const onSubmit = handleSubmit((formData) => {
    if (isSubmitting) {
      return;
    }
    setIsSubmitting(true);

    client.auth.password
      .change(formData)
      .then(() => {
        toaster.positive('Password changed', {});
        setFormError(undefined);
      })
      .catch(async (error) => {
        const errorResponse: APIErrorDataResponse = await error.response.json();
        const { errors: apiErrors } = errorResponse.error;
        if (apiErrors) {
          applyApiFormErrors(setError, apiErrors as Record<string, string>);
        } else {
          setFormError(errorResponse.error);
        }
      })
      .finally(() => setIsSubmitting(false));
  });

  return (
    <Panel>
      <Panel.Header>Change password</Panel.Header>
      <Panel.Item>
        <form onSubmit={onSubmit} noValidate>
          <FormControl error={errors.currentPassword?.message}>
            <PasswordInput
              id="currentPassword"
              name="currentPassword"
              autoComplete="current-password"
              placeholder="Current password"
              ref={register}
              inputRef={register(PASSWORD_VALIDATION)}
              error={Boolean(errors.currentPassword)}
              size={SIZE.compact}
            />
          </FormControl>

          <FormControl error={errors.newPassword?.message}>
            <PasswordInput
              id="newPassword"
              name="newPassword"
              autoComplete="new-password"
              placeholder="New password"
              size={SIZE.compact}
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
            <PasswordInput
              id="confirmNewPassword"
              name="confirmNewPassword"
              autoComplete="new-password"
              placeholder="Confirm new password"
              size={SIZE.compact}
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
            size={SIZE.compact}
          >
            Save new password
          </Button>
          {formError && <FormError error={formError} />}
        </form>
      </Panel.Item>
    </Panel>
  );
};
