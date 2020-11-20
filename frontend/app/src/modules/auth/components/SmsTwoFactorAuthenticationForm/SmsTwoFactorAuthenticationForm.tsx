import React, { useState } from 'react';
import type { PhoneNumber, TfaSetupDTO } from '@rebrowse/types';
import { AuthApi } from 'api';
import { useCodeInput } from 'shared/hooks/useCodeInput';
import { TfaSmsInputMethod } from 'modules/auth/components/TfaSmsInputMethod';
import { Button } from '@rebrowse/elements';
import { Block } from 'baseui/block';
import { SetPhoneNumberForm } from 'modules/auth/components/SetPhoneNumberForm';
import FormError from 'shared/components/FormError';

type Props = {
  phoneNumber: PhoneNumber | null;
  completeSetup?: typeof AuthApi.tfa.setup.complete;
  onCompleted?: (tfaSetup: TfaSetupDTO) => void;
};

export const SmsTwoFactorAuthenticationForm = ({
  phoneNumber: initialPhoneNumber,
  completeSetup = AuthApi.tfa.setup.complete,
  onCompleted,
}: Props) => {
  const [phoneNumber, setPhoneNumber] = useState(initialPhoneNumber);
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
      return completeSetup('sms', paramCode).then(onCompleted);
    },
    handleError: (error, setError) => {
      setError(error.error);
    },
  });

  if (!phoneNumber) {
    return (
      <SetPhoneNumberForm
        initialValue={phoneNumber}
        updatePhoneNumber={(data) =>
          AuthApi.user.updatePhoneNumber(data).then((user) => {
            setPhoneNumber(user.phoneNumber);
            return user;
          })
        }
      />
    );
  }

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

      {apiError && <FormError error={apiError} />}
    </form>
  );
};
