import React from 'react';
import AuthPageLayout from 'modules/auth/components/PageLayout';
import Head from 'next/head';
import { Block } from 'baseui/block';
import { Paragraph3 } from 'baseui/typography';
import { Button } from 'baseui/button';
import AuthApi from 'api/auth';
import { useRouter } from 'next/router';
import FormError from 'shared/components/FormError';
import FormTfaInput from 'shared/components/FormTfaInput';
import useTfaInput from 'shared/hooks/useTfaInput';

const VerificationPage = () => {
  const router = useRouter();
  const { dest = '/' } = router.query;

  const {
    code,
    codeError,
    handleSubmit,
    handleChange,
    submitButtonRef,
    isSubmitting,
    apiError,
  } = useTfaInput({
    submitAction: (data) => {
      return AuthApi.tfa
        .challengeComplete('totp', data)
        .then((_) => router.replace(dest as string));
    },
    handleError: (errorDTO, setError) => {
      if (
        errorDTO.error.message === 'TFA challenge session expired' ||
        errorDTO.error?.errors?.challengeId === 'Required'
      ) {
        router.replace(`/login?dest=${encodeURIComponent(dest as string)}`);
      } else {
        setError(errorDTO.error);
      }
    },
  });

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
            <FormTfaInput
              code={code}
              handleChange={handleChange}
              error={codeError}
            />

            <Button
              ref={submitButtonRef}
              type="submit"
              $style={{ width: '100%' }}
              isLoading={isSubmitting}
            >
              Submit
            </Button>
          </Block>
        </Block>

        {apiError && !codeError && <FormError error={apiError} />}
      </form>
    </AuthPageLayout>
  );
};

export default VerificationPage;
