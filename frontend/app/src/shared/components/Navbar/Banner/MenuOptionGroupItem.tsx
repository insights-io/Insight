import React from 'react';
import { Block } from 'baseui/block';
import type { AvatarDTO, User } from '@rebrowse/types';
import Link from 'next/link';
import { ACCOUNT_SETTINGS_DETAILS_PAGE } from 'shared/constants/routes';
import { UnstyledLink } from '@rebrowse/elements';

import { OrganizationAvatar } from '../../OrganizationAvatar';
import { UserAvatar } from '../../OrganizationAvatar/OrganizationAvatar';

import { BannerCard } from './BannerCard';

export type Props = Pick<User, 'email' | 'fullName'> & {
  children: 'Account' | 'Organization';
  organizationName: string | undefined;
  organizationAvatar: AvatarDTO | undefined;
};

export const MenuOptionGroupItem = ({
  email,
  fullName,
  children,
  organizationName,
  organizationAvatar,
}: Props) => {
  return (
    <Block as="li" padding="12px">
      {children === 'Organization' ? (
        <BannerCard
          subtitle={fullName || email}
          avatar={
            <OrganizationAvatar
              name={organizationName}
              avatar={organizationAvatar}
            />
          }
          title={organizationName || 'My Organization'}
        />
      ) : (
        <Link href={ACCOUNT_SETTINGS_DETAILS_PAGE}>
          <UnstyledLink>
            <BannerCard
              subtitle={email}
              title={fullName || 'My Account'}
              avatar={<UserAvatar fullName={fullName} email={email} />}
            />
          </UnstyledLink>
        </Link>
      )}
    </Block>
  );
};
