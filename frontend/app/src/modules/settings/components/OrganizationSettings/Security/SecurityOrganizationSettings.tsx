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
import React, { useState, useEffect } from 'react';
import { useForm, Controller } from 'react-hook-form';
import FormError from 'shared/components/FormError';
import useSWRQuery from 'shared/hooks/useSWRQuery';
import { createInputOverrides } from 'shared/styles/input';

type Props = {
  organization: Organization | undefined;
  isLoading: boolean;
};

type SsoMethodValue = { label: string; id: SsoMethod };

type SsoSetupFormData = {
  method: SsoMethodValue;
  configurationEndpoint?: string;
};

const SAML_METHOD = { label: 'SAML', id: 'saml' } as const;

const options: SsoMethodValue[] = [
  SAML_METHOD,
  { label: 'Google', id: 'google' },
  { label: 'Microsoft', id: 'microsoft' },
  { label: 'Github', id: 'github' },
];

const SecurityOrganizationSettings = ({
  organization: _organization,
  isLoading: _isLoading,
}: Props) => {
  const [_css, theme] = useStyletron();
  const inputOverrides = createInputOverrides(theme);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [formError, setFormError] = useState<APIError | undefined>();
  const {
    isLoading,
    mutate,
    data: maybeSsoSetup,
  } = useSWRQuery('AuthApi.sso.setup.get', () => AuthApi.sso.setup.get());

  const { register, handleSubmit, errors, control, setValue, watch } = useForm<
    SsoSetupFormData
  >({ defaultValues: { method: options[0] } });

  useEffect(() => {
    if (maybeSsoSetup) {
      setValue(
        'method',
        options.find((o) => o.id === maybeSsoSetup.data.method)
      );
      setValue(
        'configurationEndpoint',
        maybeSsoSetup.data.configurationEndpoint
      );
    }
  }, [maybeSsoSetup, setValue]);

  const onSubmit = handleSubmit((data) => {
    if (isSubmitting) {
      return;
    }

    console.log(data);

    setIsSubmitting(true);
    AuthApi.sso.setup
      .create(data.method.id, data.configurationEndpoint)
      .then((dataResponse) => {
        toaster.positive('SSO setup complete', {});
        mutate(dataResponse);
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
            disabled={isLoading}
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
