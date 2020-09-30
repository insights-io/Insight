import { StatefulMenu } from 'baseui/menu';
import { PLACEMENT, StatefulPopover } from 'baseui/popover';
import { Theme } from 'baseui/theme';
import React, { useMemo } from 'react';
import useHover from 'shared/hooks/useHover';
import type { User } from '@insight/types';
import { Block } from 'baseui/block';
import { VerticalAligned, expandBorderRadius } from '@insight/elements';
import { ChevronDown } from 'baseui/icon';
import {
  ACCOUNT_SETTINGS_AUTH_TOKENS_PAGE,
  ACCOUNT_SETTINGS_DETAILS_PAGE,
  ORGANIZATION_SETTINGS_AUTH_PAGE,
  ORGANIZATION_SETTINGS_BILLING_USAGE_AND_PAYMENTS_PAGE,
  ORGANIZATION_SETTINGS_MEMBERS_PAGE,
  LOGIN_PAGE,
} from 'shared/constants/routes';
import {
  ACCOUNT_SETTINGS_SECTION,
  MEMBERS_SECTION,
  ORGANIZATION_SETTINGS_SECTION,
  USAGE_AND_BILLING_SECTION,
  AUTH_TOKENS_SECTION,
  SIGN_OUT_SECTION,
} from 'shared/constants/copy';
import { AuthApi } from 'api';
import Router from 'next/router';

import { BannerCard } from './BannerCard';
import {
  MenuOptionGroupItem,
  Props as MenuOptionGroupItemProps,
} from './MenuOptionGroupItem';
import { MenuOptionItem } from './MenuOptionItem';

type Props = {
  theme: Theme;
  organizationName: string;
  user: User;
  expanded: boolean;
};

export const NavbarBanner = ({
  user,
  organizationName,
  expanded,
  theme,
}: Props) => {
  const [isHovered, callbackRef] = useHover();
  const borderRadius = useMemo(() => expandBorderRadius('8px'), []);
  const MENU_ITEMS = useMemo(() => {
    return {
      __ungrouped: [],
      Organization: [
        {
          label: ORGANIZATION_SETTINGS_SECTION,
          link: ORGANIZATION_SETTINGS_AUTH_PAGE,
        },
        {
          label: MEMBERS_SECTION,
          link: ORGANIZATION_SETTINGS_MEMBERS_PAGE,
        },
        {
          label: USAGE_AND_BILLING_SECTION,
          link: ORGANIZATION_SETTINGS_BILLING_USAGE_AND_PAYMENTS_PAGE,
        },
      ],
      Account: [
        {
          label: ACCOUNT_SETTINGS_SECTION,
          link: ACCOUNT_SETTINGS_DETAILS_PAGE,
        },
        {
          label: AUTH_TOKENS_SECTION,
          link: ACCOUNT_SETTINGS_AUTH_TOKENS_PAGE,
        },
        {
          label: SIGN_OUT_SECTION,
          link: '#',
          onClick: () =>
            AuthApi.sso.session.logout().finally(() => Router.push(LOGIN_PAGE)),
        },
      ],
    };
  }, []);

  return (
    <StatefulPopover
      focusLock
      placement={PLACEMENT.bottomRight}
      overrides={{
        Inner: { style: borderRadius },
        Body: {
          style: { zIndex: 2, ...borderRadius },
          props: { className: 'banner--menu' },
        },
      }}
      content={({ close }) => (
        <StatefulMenu
          items={MENU_ITEMS}
          onItemSelect={() => close()}
          overrides={{
            ListItem: { component: MenuOptionItem },
            OptgroupHeader: {
              component: MenuOptionGroupItem,
              props: {
                user,
                organizationName,
                overrides: {
                  OrganizationBanner: { Avatar: { style: borderRadius } },
                },
              } as MenuOptionGroupItemProps,
            },
            List: { style: { width: '250px', ...borderRadius } },
          }}
        />
      )}
    >
      <Block ref={callbackRef} className="banner">
        <BannerCard
          title={organizationName}
          titleExtra={
            <VerticalAligned marginLeft={theme.sizing.scale200}>
              <ChevronDown />
            </VerticalAligned>
          }
          subtitle={user.fullName}
          expanded={expanded}
          ref={callbackRef}
          overrides={{
            Avatar: { style: borderRadius },
            Root: { style: { ':hover': { cursor: 'pointer' } } },
            Subtitle: isHovered
              ? { style: { color: theme.colors.white } }
              : undefined,
          }}
        />
      </Block>
    </StatefulPopover>
  );
};
