import React, { useState, useRef } from 'react';
import AuthPageLayout from 'modules/auth/components/PageLayout';
import Head from 'next/head';
import { PinCode } from 'baseui/pin-code';
import { FormControl } from 'baseui/form-control';
import { Block } from 'baseui/block';
import { Paragraph3 } from 'baseui/typography';
import { Button } from 'baseui/button';
import { APIError } from '@insight/types';

type FormData = {
  code: string[];
};

const VerificationPage = () => {
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [code, setCode] = React.useState(() => Array(6).fill(''));
  const [formError, setFormError] = useState<APIError | undefined>();
  const buttonRef = useRef<HTMLButtonElement>(null);

  const handleSubmit = (values: string[]) => {
    if (isSubmitting) {
      return;
    }

    setIsSubmitting(true);

    setFormError({
      message: 'Invalid code',
      reason: 'Bad Request',
      statusCode: 400,
    });
  };

  const handleChange = (values: string[]) => {
    setCode(values);
    if (!values.includes('')) {
      buttonRef.current?.focus();
      handleSubmit(values);
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

      <form onSubmit={() => handleSubmit(code)} noValidate>
        <Block display="flex" justifyContent="center">
          <Block width="fit-content">
            <FormControl
              label="Google verification code"
              error={formError?.message}
            >
              <PinCode
                error={formError !== undefined}
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
      </form>
    </AuthPageLayout>
  );
};

export default VerificationPage;
