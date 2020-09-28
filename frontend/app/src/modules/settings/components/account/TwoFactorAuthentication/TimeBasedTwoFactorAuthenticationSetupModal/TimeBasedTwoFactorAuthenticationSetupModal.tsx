import React, { useState, useEffect } from 'react';
import { Modal, ModalHeader, ModalBody, ModalFooter } from 'baseui/modal';
import AuthApi from 'api/auth';
import { Block } from 'baseui/block';
import { Button } from 'baseui/button';
import { CodeInput } from '@insight/elements';
import useCodeInput from 'shared/hooks/useCodeInput';
import { APIError, APIErrorDataResponse } from '@insight/types';
import FormError from 'shared/components/FormError';
import { Skeleton } from 'baseui/skeleton';

import { Props } from './types';

const TimeBasedTwoFactorAuthenticationSetupModal = ({
  isOpen,
  onClose,
  onTfaConfigured,
}: Props) => {
  const [setupStartError, setSetupStartError] = useState<APIError>();
  const [qrImage, setQrImage] = useState<string>();
  const {
    handleChange,
    handleSubmit,
    code,
    codeError,
    submitButtonRef,
    isSubmitting,
    apiError,
  } = useCodeInput({
    submitAction: (data) => {
      return AuthApi.tfa.setupComplete('totp', data).then(onTfaConfigured);
    },
    handleError: (error, setError) => {
      setError(error.error);
    },
  });

  useEffect(() => {
    if (!qrImage && isOpen) {
      AuthApi.tfa.totp
        .setupStart()
        .then((dataResponse) => setQrImage(dataResponse.data.qrImage))
        .catch(async (error) => {
          const errorDTO: APIErrorDataResponse = await error.response.json();
          setSetupStartError(errorDTO.error);
        });
    }
  }, [qrImage, setQrImage, isOpen]);

  return (
    <Modal onClose={onClose} isOpen={isOpen}>
      <form
        noValidate
        onSubmit={(event) => {
          event.preventDefault();
          handleSubmit(code);
        }}
      >
        <Block display="flex" justifyContent="center">
          <ModalHeader>Setup two factor authentication</ModalHeader>
        </Block>
        <ModalBody>
          <Block display="flex" justifyContent="center" marginBottom="24px">
            {qrImage ? (
              <img
                src={`data:image/jpeg;base64,${qrImage}`}
                alt="TFA QR code"
              />
            ) : (
              <Skeleton width="200px" height="200px" />
            )}
          </Block>
          <Block display="flex" justifyContent="center">
            <Block>
              <CodeInput
                label="Google verification code"
                disabled={setupStartError !== undefined}
                code={code}
                handleChange={handleChange}
                error={codeError}
              />
            </Block>
          </Block>
        </ModalBody>
        <ModalFooter>
          <Button
            disabled={setupStartError !== undefined}
            ref={submitButtonRef}
            type="submit"
            isLoading={isSubmitting}
            $style={{ width: '100%' }}
          >
            Submit
          </Button>
        </ModalFooter>
        {setupStartError && <FormError error={setupStartError} />}
        {apiError && !codeError && <FormError error={apiError} />}
      </form>
    </Modal>
  );
};

export default React.memo(TimeBasedTwoFactorAuthenticationSetupModal);
