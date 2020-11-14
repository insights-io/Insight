import React from 'react';
import type { TfaSetupDTO } from '@insight/types';
import { AuthApi } from 'api';
import { useCodeInput } from 'shared/hooks/useCodeInput';
import { TfaSmsInputMethod } from 'modules/auth/components/TfaSmsInputMethod';
import { Button } from '@insight/elements';
import { Block } from 'baseui/block';

type Props = {
  setupComplete?: typeof AuthApi.tfa.setup.complete;
  onSetupComplete?: (tfaSetup: TfaSetupDTO) => void;
};

export const SmsTwoFactorAuthenticationForm = ({
  setupComplete = AuthApi.tfa.setup.complete,
  onSetupComplete,
}: Props) => {
  const {
    code,
    handleChange,
    submitButtonRef,
    codeError,
    isSubmitting,
    handleSubmit,
  } = useCodeInput({
    submitAction: (paramCode) => {
      return setupComplete('sms', paramCode).then(onSetupComplete);
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
      <TfaSmsInputMethod
        code={code}
        error={codeError}
        handleChange={handleChange}
        sendCode={AuthApi.tfa.setup.sms.sendCode}
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
    </form>
  );
};
