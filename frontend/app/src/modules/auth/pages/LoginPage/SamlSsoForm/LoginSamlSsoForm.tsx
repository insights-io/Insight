import { useStyletron } from 'baseui';
import { Block } from 'baseui/block';
import { Button } from 'baseui/button';
import { FormControl } from 'baseui/form-control';
import { Input } from 'baseui/input';
import { EMAIL_VALIDATION } from 'modules/auth/validation/email';
import React, { useState, useMemo } from 'react';
import { createInputOverrides } from 'shared/styles/input';
import { locationAssign } from 'shared/utils/window';

import { samlIntegrationHrefBuilder } from '../utils';

type Props = { encodedRedirect: string };

const LoginSamlSsoForm = ({ encodedRedirect }: Props) => {
  const [_css, theme] = useStyletron();
  const [isSubmitting] = useState(false);
  const [email, setEmail] = useState('');
  const inputOverrides = createInputOverrides(theme);
  const encodedEmail = encodeURIComponent(email);

  const error = useMemo(() => {
    if (!email) {
      return undefined;
    }
    const emailPatternMatch = email.match(EMAIL_VALIDATION.pattern.value);
    if (!emailPatternMatch) {
      return EMAIL_VALIDATION.pattern.message;
    }
    return undefined;
  }, [email]);

  return (
    <form
      onSubmit={(event) => {
        event.stopPropagation();
        event.preventDefault();
        locationAssign(
          samlIntegrationHrefBuilder(encodedRedirect, encodedEmail)
        );
      }}
      noValidate
    >
      <Block>
        <FormControl label="Work Email" error={error}>
          <Input
            overrides={inputOverrides}
            id="email"
            name="email"
            type="email"
            placeholder="user@company.com"
            required
            value={email}
            onChange={(event) => setEmail(event.currentTarget.value)}
            error={Boolean(error)}
          />
        </FormControl>
      </Block>

      <Button
        type="submit"
        $style={{ width: '100%' }}
        isLoading={isSubmitting}
        disabled={Boolean(error) || !email}
      >
        Sign in
      </Button>
    </form>
  );
};

export default React.memo(LoginSamlSsoForm);
