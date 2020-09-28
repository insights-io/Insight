import React, { useEffect, useState } from 'react';
import { Card } from 'baseui/card';
import { FormControl } from 'baseui/form-control';
import { Controller, useForm } from 'react-hook-form';
import { Select } from 'baseui/select';
import { Input } from 'baseui/input';
import { Button, SHAPE } from 'baseui/button';
import FormError from 'shared/components/FormError';
import { REQUIRED_VALIDATION } from 'modules/auth/validation/base';
import { useStyletron } from 'baseui';
import { createInputOverrides } from 'shared/styles/input';
import { AuthApi } from 'api';
import { toaster } from 'baseui/toast';
import type {
  APIError,
  APIErrorDataResponse,
  SsoSetup,
  SsoSetupDTO,
} from '@insight/types';

import { SsoMethodSelectValue, SSO_OPTIONS, SsoSetupFormData } from './utils';

type Props = {
  maybeSsoSetup: SsoSetup | undefined;
  setSsoSetup: (ssoSetup: SsoSetupDTO) => void;
};

export const AuthenticationSetup = ({ maybeSsoSetup, setSsoSetup }: Props) => {
  const { register, handleSubmit, errors, control, setValue, watch } = useForm<
    SsoSetupFormData
  >({ defaultValues: { method: SSO_OPTIONS[0] } });

  const [_css, theme] = useStyletron();
  const inputOverrides = createInputOverrides(theme);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [formError, setFormError] = useState<APIError | undefined>();

  useEffect(() => {
    if (maybeSsoSetup) {
      setValue(
        'method',
        SSO_OPTIONS.find((o) => o.id === maybeSsoSetup.method)
      );
      setValue('configurationEndpoint', maybeSsoSetup.configurationEndpoint);
    }
  }, [maybeSsoSetup, setValue]);

  const onSubmit = handleSubmit((data) => {
    if (isSubmitting) {
      return;
    }

    setIsSubmitting(true);

    AuthApi.sso.setup
      .create(data.method.id, data.configurationEndpoint)
      .then((dataResponse) => {
        toaster.positive('SSO setup complete', {});
        setSsoSetup(dataResponse);
        setFormError(undefined);
      })
      .catch(async (setupError) => {
        const errorDTO: APIErrorDataResponse = await setupError.response.json();
        setFormError(errorDTO.error);
      })
      .finally(() => setIsSubmitting(false));
  });

  const configurationEndpointError =
    errors.configurationEndpoint?.message ||
    formError?.errors?.configurationEndpoint;

  const methodError = formError?.errors?.method;

  return (
    <Card>
      <form noValidate onSubmit={onSubmit}>
        <FormControl label="Method" error={methodError}>
          <Controller
            control={control}
            name="method"
            render={({ onChange, value }) => (
              <Select
                clearable={false}
                options={SSO_OPTIONS}
                placeholder="Select method"
                value={[value]}
                error={Boolean(methodError)}
                onChange={(params) => {
                  onChange(params.value[0] as SsoMethodSelectValue);
                }}
              />
            )}
          />
        </FormControl>

        {watch('method').id === 'saml' && (
          <FormControl
            label="Configuration endpoint"
            caption="We will fetch required metadata from this URL to setup the SSO integration"
            error={configurationEndpointError}
          >
            <Input
              overrides={inputOverrides}
              id="configurationEndpoint"
              name="configurationEndpoint"
              placeholder="https://example.okta.com/app/exkw843tlucjMJ0kL4x6/sso/saml/metadata"
              required
              inputRef={register(REQUIRED_VALIDATION)}
              error={Boolean(configurationEndpointError)}
            />
          </FormControl>
        )}
        <Button
          type="submit"
          isLoading={isSubmitting}
          shape={SHAPE.pill}
          $style={{ width: '100%' }}
        >
          Setup
        </Button>
        {formError && <FormError error={formError} />}
      </form>
    </Card>
  );
};
