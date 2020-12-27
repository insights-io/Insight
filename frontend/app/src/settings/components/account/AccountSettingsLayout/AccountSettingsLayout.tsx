import React from 'react';
import {
  ACCOUNT_SETTINGS_AUTH_TOKENS_PAGE,
  ACCOUNT_SETTINGS_DETAILS_PAGE,
  ACCOUNT_SETTINGS_SECURITY_PAGE,
} from 'shared/constants/routes';
import { SettingsLayout } from 'settings/components/SettingsLayout';
import { SETTINGS_SEARCH_OPTIONS } from 'settings/constants';
import type { SettingsLayoutPropsBase, SidebarSection } from 'settings/types';

export const SIDEBAR_SECTIONS: SidebarSection[] = [
  {
    header: 'Account',
    items: [
      { text: 'Details', link: ACCOUNT_SETTINGS_DETAILS_PAGE },
      { text: 'Security', link: ACCOUNT_SETTINGS_SECURITY_PAGE },
    ],
  },
  {
    header: 'Api',
    items: [
      {
        text: 'Auth Tokens',
        link: ACCOUNT_SETTINGS_AUTH_TOKENS_PAGE,
      },
    ],
  },
];

type Props = SettingsLayoutPropsBase;

export const AccountSettingsLayout = (props: Props) => {
  return (
    <SettingsLayout
      {...props}
      searchOptions={SETTINGS_SEARCH_OPTIONS}
      sidebarSections={SIDEBAR_SECTIONS}
    />
  );
};
