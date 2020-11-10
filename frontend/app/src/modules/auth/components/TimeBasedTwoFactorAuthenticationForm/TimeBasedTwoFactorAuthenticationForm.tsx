import React, { useState, useEffect } from 'react';
import { AuthApi } from 'api/auth';
import { Block } from 'baseui/block';
import { CodeInput, Button, Flex } from '@insight/elements';
import { useCodeInput } from 'shared/hooks/useCodeInput';
import type {
  APIError,
  APIErrorDataResponse,
  TfaSetupDTO,
} from '@insight/types';
import FormError from 'shared/components/FormError';
import { Skeleton } from 'baseui/skeleton';
import { Paragraph3 } from 'baseui/typography';

type Props = {
  onTfaConfigured: (tfaSetup: TfaSetupDTO) => void;
};

export const TimeBasedTwoFactorAuthenticationForm = ({
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
      return AuthApi.tfa.setup.complete('totp', data).then(onTfaConfigured);
    },
    handleError: (error, setError) => {
      setError(error.error);
    },
  });

  useEffect(() => {
    if (!qrImage) {
      AuthApi.tfa.setup.totp
        .start()
        .then((dataResponse) => setQrImage(dataResponse.data.qrImage))
        .catch(async (error) => {
          const errorDTO: APIErrorDataResponse = await error.response.json();
          setSetupStartError(errorDTO.error);
        });
    }
  }, [qrImage, setQrImage]);

  return (
    <form
      noValidate
      onSubmit={(event) => {
        event.preventDefault();
        handleSubmit(code);
      }}
    >
      <Flex justifyContent="center">
        <Paragraph3>Scan QR code to start</Paragraph3>
      </Flex>

      <Flex justifyContent="center" marginBottom="24px">
        {qrImage ? (
          <img src={`data:image/jpeg;base64,${qrImage}`} alt="TFA QR code" />
        ) : (
          <Skeleton width="200px" height="200px" />
        )}
      </Flex>
      <Flex justifyContent="center">
        <Block>
          <CodeInput
            label="Google verification code"
            disabled={setupStartError !== undefined}
            code={code}
            handleChange={handleChange}
            error={codeError}
          />
        </Block>
      </Flex>
      <Block marginTop="32px">
        <Button
          disabled={setupStartError !== undefined}
          ref={submitButtonRef}
          type="submit"
          isLoading={isSubmitting}
          $style={{ width: '100%' }}
        >
          Submit
        </Button>
      </Block>
      {setupStartError && <FormError error={setupStartError} />}
      {apiError && !codeError && <FormError error={apiError} />}
    </form>
  );
};
