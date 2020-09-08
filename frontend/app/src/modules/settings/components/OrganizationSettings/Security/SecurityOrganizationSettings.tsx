import {
  APIError,
  APIErrorDataResponse,
  Organization,
  SsoMethod,
} from '@insight/types';
import { AuthApi } from 'api';
import { useStyletron } from 'baseui';
import { Block } from 'baseui/block';
import { Button, SHAPE } from 'baseui/button';
import { Card } from 'baseui/card';
import { FormControl } from 'baseui/form-control';
import { Input } from 'baseui/input';
import { Select } from 'baseui/select';
import { toaster } from 'baseui/toast';
import { REQUIRED_VALIDATION } from 'modules/auth/validation/base';
import React, { useState } from 'react';
import { useForm, Controller } from 'react-hook-form';
import FormError from 'shared/components/FormError';
import { createInputOverrides } from 'shared/styles/input';

type Props = {
  organization: Organization | undefined;
  isLoading: boolean;
};

type SsoMethodValue = { label: string; id: SsoMethod };

type SsoSetupFormData = {
  configurationEndpoint: string;
  method: SsoMethodValue;
};

const options: SsoMethodValue[] = [{ label: 'SAML', id: 'saml' }];

const SecurityOrganizationSettings = ({
  organization: _organization,
  isLoading: _isLoading,
}: Props) => {
  const [_css, theme] = useStyletron();
  const inputOverrides = createInputOverrides(theme);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [formError, setFormError] = useState<APIError | undefined>();

  const { register, handleSubmit, errors, control } = useForm<SsoSetupFormData>(
    { defaultValues: { method: options[0] } }
  );

  const onSubmit = handleSubmit((data) => {
    if (isSubmitting) {
      return;
    }

    setIsSubmitting(true);
    AuthApi.ssoSetup
      .create(data.method.id, data.configurationEndpoint)
      .then((_) => {
        toaster.positive('SSO setup complete', {});
      })
      .catch(async (error) => {
        const errorDTO: APIErrorDataResponse = await error.response.json();
        setFormError(errorDTO.error);
      })
      .finally(() => setIsSubmitting(false));
  });

  const configurationEndpointError =
    errors.configurationEndpoint?.message ||
    formError?.errors?.configurationEndpoint;

  const methodError = formError?.errors?.method;

  return (
    <Block>
      <Card title="SSO">
        <form noValidate onSubmit={onSubmit}>
          <FormControl label="Method" error={methodError}>
            <Controller
              control={control}
              name="method"
              render={({ onChange, value }) => (
                <Select
                  clearable={false}
                  options={options}
                  placeholder="Select method"
                  value={[value]}
                  error={Boolean(methodError)}
                  onChange={(params) => {
                    onChange(params.value[0] as SsoMethodValue);
                  }}
                />
              )}
            />
          </FormControl>

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
    </Block>
  );
};

export default React.memo(SecurityOrganizationSettings);
