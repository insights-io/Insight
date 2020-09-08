import { APIError, APIErrorDataResponse } from '@insight/types';
import { AuthApi } from 'api';
import { useStyletron } from 'baseui';
import { Block } from 'baseui/block';
import { Button } from 'baseui/button';
import { FormControl } from 'baseui/form-control';
import { Input } from 'baseui/input';
import { EMAIL_VALIDATION } from 'modules/auth/validation/email';
import React, { useState, useMemo } from 'react';
import FormError from 'shared/components/FormError';
import { createInputOverrides } from 'shared/styles/input';
import { locationAssign } from 'shared/utils/window';

import { samlIntegrationHrefBuilder } from '../utils';

type Props = { encodedRedirect: string };

const LoginSamlSsoForm = ({ encodedRedirect }: Props) => {
  const [_css, theme] = useStyletron();
  const [email, setEmail] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const inputOverrides = createInputOverrides(theme);
  const [formError, setFormError] = useState<APIError | undefined>();
  const [setupExists, setSetupExists] = useState(true);

  const validationError = useMemo(() => {
    if (!email) {
      return undefined;
    }
    const emailPatternMatch = email.match(EMAIL_VALIDATION.pattern.value);
    if (!emailPatternMatch) {
      return EMAIL_VALIDATION.pattern.message;
    }
    return undefined;
  }, [email]);

  const onSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.stopPropagation();
    event.preventDefault();

    if (isSubmitting) {
      return;
    }

    const domain = email.split('@')[1];
    setIsSubmitting(true);

    AuthApi.sso.setup
      .getByDomain(domain)
      .then((dataRepsonse) => {
        if (dataRepsonse.data) {
          const encodedEmail = encodeURIComponent(email);
          const location = samlIntegrationHrefBuilder(
            encodedRedirect,
            encodedEmail
          );
          locationAssign(location);
        } else {
          setSetupExists(dataRepsonse.data);
        }
      })
      .catch(async (error) => {
        const errorDTO: APIErrorDataResponse = await error.response.json();
        setFormError(errorDTO.error);
      })
      .finally(() => {
        setIsSubmitting(false);
        setFormError(undefined);
      });
  };

  return (
    <form onSubmit={onSubmit} noValidate>
      <Block>
        <FormControl label="Work Email" error={validationError}>
          <Input
            overrides={inputOverrides}
            id="email"
            name="email"
            type="email"
            placeholder="user@company.com"
            required
            value={email}
            onChange={(event) => setEmail(event.currentTarget.value)}
            error={Boolean(validationError)}
          />
        </FormControl>
      </Block>

      <Button
        type="submit"
        $style={{ width: '100%' }}
        isLoading={isSubmitting}
        disabled={Boolean(validationError) || !email}
      >
        Sign in
      </Button>
      {formError && <FormError error={formError} />}
      {!setupExists && (
        <FormError
          error={
            {
              message: 'That email or domain isn’t registered for SSO.',
            } as APIError
          }
        />
      )}
    </form>
  );
};

export default React.memo(LoginSamlSsoForm);
