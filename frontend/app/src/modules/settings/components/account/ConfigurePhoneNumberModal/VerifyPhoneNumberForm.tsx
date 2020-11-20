import React from 'react';
import type { UserDTO } from '@rebrowse/types';
import { AuthApi } from 'api';
import { Block } from 'baseui/block';
import { Button, SHAPE, SIZE } from 'baseui/button';
import { toaster } from 'baseui/toast';
import { TfaSmsInputMethod } from 'modules/auth/components/TfaSmsInputMethod';
import { Flex } from '@rebrowse/elements';
import { useCodeInput } from 'shared/hooks/useCodeInput';

type Props = {
  onBack: () => void;
  onPhoneNumberVerified: (user: UserDTO) => void;
};

const VerifyPhoneNumberForm = ({ onBack, onPhoneNumberVerified }: Props) => {
  const {
    code,
    handleChange,
    submitButtonRef,
    codeError,
    isSubmitting,
    handleSubmit,
  } = useCodeInput({
    submitAction: (paramCode) => {
      return AuthApi.user.phoneNumberVerify(paramCode).then((user) => {
        toaster.positive(`Phone number verified`, {});
        onPhoneNumberVerified(user);
      });
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
      <Block>
        <TfaSmsInputMethod
          code={code}
          error={codeError}
          handleChange={handleChange}
          sendCode={AuthApi.user.phoneNumberVerifySendCode}
        />
      </Block>
      <Flex justifyContent="flex-end" marginTop="16px">
        <Button
          onClick={onBack}
          kind="secondary"
          shape={SHAPE.pill}
          size={SIZE.compact}
        >
          Back
        </Button>
        <Button
          type="submit"
          ref={submitButtonRef}
          isLoading={isSubmitting}
          shape={SHAPE.pill}
          size={SIZE.compact}
          $style={{ flex: 1, marginLeft: '16px' }}
        >
          Submit
        </Button>
      </Flex>
    </form>
  );
};

export default VerifyPhoneNumberForm;
