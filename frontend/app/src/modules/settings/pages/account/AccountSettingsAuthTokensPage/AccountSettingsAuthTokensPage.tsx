import React from 'react';
import {
  SETTINGS_PATH_PART,
  ACCOUNT_SETTINGS_AUTH_TOKENS_PATH_PART,
  ACCOUNT_SETTINGS_PATH_PART,
} from 'shared/constants/routes';
import { AccountSettingsPageLayout } from 'modules/settings/components/account/AccountSettingsPageLayout';
import type { Path } from 'modules/settings/types';

const PATH: Path = [
  SETTINGS_PATH_PART,
  ACCOUNT_SETTINGS_PATH_PART,
  ACCOUNT_SETTINGS_AUTH_TOKENS_PATH_PART,
];

export const AccountSettingsAuthTokensPage = () => {
  return (
    <AccountSettingsPageLayout path={PATH} header="Auth Tokens">
      <div>Coming soon.</div>
    </AccountSettingsPageLayout>
  );
};
