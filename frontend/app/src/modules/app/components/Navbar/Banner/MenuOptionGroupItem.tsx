import React from 'react';
import { Block } from 'baseui/block';
import type { User } from '@insight/types';
import Link from 'next/link';
import { ACCOUNT_SETTINGS_DETAILS_PAGE } from 'shared/constants/routes';
import { UnstyledLink } from '@insight/elements';

import { BannerCard, Props as BannerCardProps } from './BannerCard';

export type Props = {
  children: 'Account' | 'Organization';
  user: User;
  organizationName: string;
  overrides?: {
    OrganizationBanner?: BannerCardProps['overrides'];
  };
};

export const MenuOptionGroupItem = ({
  user,
  children,
  organizationName,
  overrides,
}: Props) => {
  return (
    <Block as="li" padding="12px">
      {children === 'Organization' ? (
        <BannerCard
          subtitle={user.fullName}
          title={organizationName}
          overrides={overrides?.OrganizationBanner}
        />
      ) : (
        <Link href={ACCOUNT_SETTINGS_DETAILS_PAGE}>
          <UnstyledLink>
            <BannerCard subtitle={user.email} title={user.fullName} />
          </UnstyledLink>
        </Link>
      )}
    </Block>
  );
};
