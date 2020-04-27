import React from 'react';
import Link from 'next/link';
import AuthPageLayout from 'modules/auth/components/PageLayout';
import { useStyletron } from 'baseui';
import { Paragraph4 } from 'baseui/typography';

const InvalidSignUpPage = () => {
  const [css, theme] = useStyletron();
  return (
    <AuthPageLayout subtitle="Hmm.">
      <Paragraph4>
        It looks like this invite is invalid or has already been accepted.
      </Paragraph4>
      <Link href="/login">
        <a
          className={css({
            color: 'white',
            textDecoration: 'underline',
            marginTop: theme.sizing.scale400,
          })}
        >
          Log in or reset your password.
        </a>
      </Link>
    </AuthPageLayout>
  );
};

export default InvalidSignUpPage;
