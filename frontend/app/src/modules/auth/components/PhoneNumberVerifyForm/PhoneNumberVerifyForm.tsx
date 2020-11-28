import React from 'react';
import { Button } from '@rebrowse/elements';
import { Block } from 'baseui/block';
import FormError from 'shared/components/FormError';
import { useCodeInput } from 'shared/hooks/useCodeInput';
import type { CodeValidityDTO } from '@rebrowse/types';
import { SmsMfaInputMethod } from 'modules/auth/components/SmsMfaInputMethod';

type Props<T> = {
  verify: (code: number) => Promise<T>;
  sendCode: () => Promise<CodeValidityDTO>;
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
    submitAction: (paramCode) => {
      return verify(paramCode);
    },
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
      <SmsMfaInputMethod
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
          Submit
        </Button>
      </Block>

      {apiError && <FormError error={apiError} />}
    </form>
  );
};
