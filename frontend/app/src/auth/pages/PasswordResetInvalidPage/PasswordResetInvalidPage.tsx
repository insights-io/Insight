import React from 'react';
import Link from 'next/link';
import { AuthPageLayout } from 'auth/components/PageLayout';
import { useStyletron } from 'baseui';
import { Button } from 'baseui/button';
import { Paragraph3 } from 'baseui/typography';

export const PasswordResetInvalidPage = () => {
  const [css, theme] = useStyletron();
  return (
    <AuthPageLayout subtitle="Oooops.">
      <Paragraph3 $style={{ textAlign: 'center' }}>
        It looks like this password reset request is invalid or has already been
        accepted.
      </Paragraph3>
      <Link href="/login">
        <a className={css({ textDecoration: 'none' })}>
          <Button $style={{ width: '100%', marginTop: theme.sizing.scale700 }}>
            Log in or reset your password
          </Button>
        </a>
      </Link>
    </AuthPageLayout>
  );
};
