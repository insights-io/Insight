import React from 'react';
import { Button, useCodeInput } from '@rebrowse/elements';
import { Block } from 'baseui/block';
import { FormError } from 'shared/components/FormError';
import type { CodeValidityDTO } from '@rebrowse/types';
import type { HttpResponse } from '@rebrowse/sdk';
import { SmsMfaInput } from 'signin/components/SmsMfaInput';

type Props<T> = {
  verify: (code: number) => Promise<T>;
  sendCode: () => Promise<HttpResponse<CodeValidityDTO>>;
};

export const PhoneNumberVerifyForm = <T,>({ verify, sendCode }: Props<T>) => {
  const {
    code,
    handleChange,
    submitButtonRef,
    codeError,
    isSubmitting,
    handleSubmit,
    apiError,
  } = useCodeInput({
    submitAction: verify,
    handleError: (error, setError) => {
      setError(error.error);
    },
  });

  return (
    <form
      noValidate
      onSubmit={(event) => {
        event.preventDefault();
        handleSubmit(code);
      }}
    >
      <SmsMfaInput
        code={code}
        error={codeError}
        handleChange={handleChange}
        sendCode={sendCode}
      />

      <Block marginTop="32px">
        <Button
          ref={submitButtonRef}
          type="submit"
          isLoading={isSubmitting}
          $style={{ width: '100%' }}
        >
          Continue
        </Button>
      </Block>

      {apiError && <FormError error={apiError} />}
    </form>
  );
};
