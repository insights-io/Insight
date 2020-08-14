import React, { useState, useRef, useMemo } from 'react';
import AuthPageLayout from 'modules/auth/components/PageLayout';
import Head from 'next/head';
import { PinCode } from 'baseui/pin-code';
import { FormControl } from 'baseui/form-control';
import { Block } from 'baseui/block';
import { Paragraph3 } from 'baseui/typography';
import { Button } from 'baseui/button';
import { APIError, APIErrorDataResponse } from '@insight/types';
import AuthApi from 'api/auth';
import { useRouter } from 'next/router';
import FormError from 'shared/components/FormError';

const VerificationPage = () => {
  const router = useRouter();
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [code, setCode] = React.useState(() => Array(6).fill(''));
  const [formError, setFormError] = useState<APIError | undefined>();
  const buttonRef = useRef<HTMLButtonElement>(null);
  const { dest = '/' } = router.query;

  const codeError = useMemo(() => {
    return formError?.errors?.code;
  }, [formError]);

  const handleSubmit = (values: string[]) => {
    if (isSubmitting) {
      return;
    }

    setIsSubmitting(true);
    setFormError(undefined);
    AuthApi.sso
      .tfaComplete(Number(values.join('')))
      .then((_) => router.replace(dest as string))
      .catch(async (error) => {
        const errorDTO: APIErrorDataResponse = await error.response.json();
        if (
          errorDTO.error.message === 'Verification session expired' ||
          errorDTO.error?.errors?.verificationId === 'Required'
        ) {
          router.replace(`/login?dest=${encodeURIComponent(dest as string)}`);
        } else {
          setFormError(errorDTO.error);
        }
      })
      .finally(() => setIsSubmitting(false));
  };

  const handleChange = (values: string[]) => {
    setCode(values);
    if (!values.includes('')) {
      buttonRef.current?.focus();
    } else {
      setFormError(undefined);
    }
  };

  return (
    <AuthPageLayout>
      <Head>
        <title>Insight | Verification</title>
      </Head>

      <Block display="flex" justifyContent="center" marginBottom="32px">
        <Paragraph3>
          To protect your account, please complete the following verification.
        </Paragraph3>
      </Block>

      <form
        noValidate
        onSubmit={(event) => {
          event.preventDefault();
          handleSubmit(code);
        }}
      >
        <Block display="flex" justifyContent="center">
          <Block width="fit-content">
            <FormControl label="Google verification code" error={codeError}>
              <PinCode
                autoFocus
                error={codeError !== undefined}
                onChange={(data) => handleChange(data.values)}
                values={code}
              />
            </FormControl>

            <Button
              ref={buttonRef}
              type="submit"
              $style={{ width: '100%' }}
              isLoading={isSubmitting}
            >
              Submit
            </Button>
          </Block>
        </Block>

        {formError && !codeError && <FormError error={formError} />}
      </form>
    </AuthPageLayout>
  );
};

export default VerificationPage;
