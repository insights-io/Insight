import React from 'react';
import { Block } from 'baseui/block';
import type { User } from '@insight/types';
import Link from 'next/link';
import { ACCOUNT_SETTINGS_DETAILS_PAGE } from 'shared/constants/routes';
import { UnstyledLink } from '@insight/elements';

import { BannerCard, Props as BannerCardProps } from './BannerCard';

export type Props = Pick<User, 'email' | 'fullName'> & {
  children: 'Account' | 'Organization';
  organizationName: string | undefined;
  overrides?: {
    OrganizationBanner?: BannerCardProps['overrides'];
  };
};

export const MenuOptionGroupItem = ({
  email,
  fullName,
  children,
  organizationName,
  overrides,
}: Props) => {
  return (
    <Block as="li" padding="12px">
      {children === 'Organization' ? (
        <BannerCard
          subtitle={fullName || email}
          title={organizationName || 'My Organization'}
          avatar={organizationName || 'O'}
          overrides={overrides?.OrganizationBanner}
        />
      ) : (
        <Link href={ACCOUNT_SETTINGS_DETAILS_PAGE}>
          <UnstyledLink>
            <BannerCard
              subtitle={email}
              title={fullName || 'My Account'}
              avatar={fullName || email}
            />
          </UnstyledLink>
        </Link>
      )}
    </Block>
  );
};
