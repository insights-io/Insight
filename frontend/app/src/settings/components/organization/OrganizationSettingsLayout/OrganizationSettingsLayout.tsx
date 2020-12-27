import React from 'react';
import {
  ORGANIZATION_SETTINGS_AUTH_PAGE,
  ORGANIZATION_SETTINGS_BILLING_SUBSCRIPTION_PAGE,
  ORGANIZATION_SETTINGS_BILLING_USAGE_AND_PAYMENTS_PAGE,
  ORGANIZATION_SETTINGS_GENERAL_PAGE,
  ORGANIZATION_SETTINGS_MEMBERS_PAGE,
  ORGANIZATION_SETTINGS_SECURITY_PAGE,
} from 'shared/constants/routes';
import { SettingsLayout } from 'settings/components/SettingsLayout';
import { SETTINGS_SEARCH_OPTIONS } from 'settings/constants';
import type { SettingsLayoutPropsBase, SidebarSection } from 'settings/types';
import { MEMBERS_SECTION } from 'shared/constants/copy';

export const SIDEBAR_SECTIONS: SidebarSection[] = [
  {
    header: 'Organization',
    items: [
      {
        text: 'General settings',
        link: ORGANIZATION_SETTINGS_GENERAL_PAGE,
      },
      {
        text: 'Security',
        link: ORGANIZATION_SETTINGS_SECURITY_PAGE,
      },
      {
        text: MEMBERS_SECTION,
        link: ORGANIZATION_SETTINGS_MEMBERS_PAGE,
      },
      { text: 'Auth', link: ORGANIZATION_SETTINGS_AUTH_PAGE },
    ],
  },
  {
    header: 'Usage & Billing',
    items: [
      {
        text: 'Subscription',
        link: ORGANIZATION_SETTINGS_BILLING_SUBSCRIPTION_PAGE,
      },
      {
        text: 'Usage & Payments',
        link: ORGANIZATION_SETTINGS_BILLING_USAGE_AND_PAYMENTS_PAGE,
      },
    ],
  },
];

type Props = SettingsLayoutPropsBase;

export const OrganizationSettingsLayout = (props: Props) => {
  return (
    <SettingsLayout
      {...props}
      searchOptions={SETTINGS_SEARCH_OPTIONS}
      sidebarSections={SIDEBAR_SECTIONS}
    />
  );
};
