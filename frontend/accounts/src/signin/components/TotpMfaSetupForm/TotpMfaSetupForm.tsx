import React from 'react';
import { Block } from 'baseui/block';
import { CodeInput, Button, Flex, useCodeInput } from '@rebrowse/elements';
import type { MfaSetupDTO } from '@rebrowse/types';
import { FormError } from 'shared/components/FormError';
import { Skeleton } from 'baseui/skeleton';
import type { AuthorizationSuccessResponse, HttpResponse } from '@rebrowse/sdk';

import { useQrImage } from './useQrCode';

export type Props = {
  completeSetup: (
    code: number
  ) => Promise<HttpResponse<AuthorizationSuccessResponse>>;
  onCompleted?: (value: AuthorizationSuccessResponse) => void;
};

export const TotpMfaSetupForm = ({ onCompleted, completeSetup }: Props) => {
  const { qrImageError, qrImage } = useQrImage();

  const {
    handleChange,
    handleSubmit,
    code,
    codeError,
    submitButtonRef,
    isSubmitting,
    apiError,
  } = useCodeInput({
    submitAction: (paramCode) => {
      return completeSetup(paramCode)
        .then((response) => response.data)
        .then(onCompleted);
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
      <Flex justifyContent="center" marginBottom="16px">
        {qrImage ? (
          <img
            src={`data:image/jpeg;base64,${qrImage}`}
            alt="Time-based one-time password QR code"
          />
        ) : (
          <Skeleton width="200px" height="200px" />
        )}
      </Flex>

      <Flex justifyContent="center">
        <CodeInput
          label="Google verification code"
          disabled={qrImageError !== undefined}
          code={code}
          handleChange={handleChange}
          error={codeError}
        />
      </Flex>

      <Block marginTop="16px">
        <Button
          type="submit"
          disabled={qrImageError !== undefined}
          ref={submitButtonRef}
          isLoading={isSubmitting}
          $style={{ width: '100%' }}
        >
          Continue
        </Button>
      </Block>
      {qrImageError && <FormError error={qrImageError} />}
      {apiError && !codeError && <FormError error={apiError} />}
    </form>
  );
};
