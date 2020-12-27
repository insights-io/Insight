import React from 'react';
import { Button, Flex, UnstyledLink } from '@rebrowse/elements';
import { AuthPageLayout } from 'auth/components/PageLayout';
import Link from 'next/link';
import { LOGIN_PAGE } from 'shared/constants/routes';

type Props = {
  expiresAt?: string;
};

export const AcceptTeamInviteInvalidPage = ({ expiresAt }: Props) => {
  const message = expiresAt
    ? `Your team invite has expired on ${new Date(
        expiresAt
      ).toLocaleDateString()}`
    : 'We could not find team invite you were looking for';

  return (
    <AuthPageLayout subtitle={message}>
      <Flex justifyContent="center">
        <Link href={LOGIN_PAGE}>
          <UnstyledLink>
            <Button>Back to login</Button>
          </UnstyledLink>
        </Link>
      </Flex>
    </AuthPageLayout>
  );
};
