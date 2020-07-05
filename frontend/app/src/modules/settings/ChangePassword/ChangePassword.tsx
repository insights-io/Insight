import React, { useState } from 'react';
import { Card, CardOverrides } from 'baseui/card';
import { Button } from 'baseui/button';
import { useForm } from 'react-hook-form';
import { FormControl } from 'baseui/form-control';
import { Input } from 'baseui/input';
import { PASSWORD_VALIDATION } from 'modules/auth/validation/password';
import { createInputOverrides } from 'shared/styles/input';
import { useStyletron } from 'baseui';
import {
  ChangePasswordDTO,
  APIError,
  APIErrorDataResponse,
} from '@insight/types';
import PasswordApi from 'api/password';
import FormError from 'shared/components/FormError';
import { toaster } from 'baseui/toast';

type Props = {
  overrides?: CardOverrides;
};

const ChangePassword = ({ overrides }: Props) => {
  const [_css, theme] = useStyletron();
  const { register, handleSubmit, errors, watch } = useForm<
    ChangePasswordDTO
  >();
  const [isSubmitting, setIsSubmitting] = useState(false);
  const inputOverrides = createInputOverrides(theme);
  const [formError, setFormError] = useState<APIError | undefined>();

  const onSubmit = handleSubmit((formData) => {
    if (isSubmitting) {
      return;
    }
    setIsSubmitting(true);

    PasswordApi.change(formData)
      .then((_resp) => {
        toaster.positive('Password changed', {});
        setFormError(undefined);
      })
      .catch(async (error) => {
        const errorDTO: APIErrorDataResponse = await error.response.json();
        setFormError(errorDTO.error);
      })
      .finally(() => setIsSubmitting(false));
  });

  return (
    <Card title="Change password" overrides={overrides}>
      <form onSubmit={onSubmit} noValidate>
        <FormControl error={errors.currentPassword?.message}>
          <Input
            overrides={inputOverrides}
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
            overrides={inputOverrides}
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
            overrides={inputOverrides}
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

export default ChangePassword;
