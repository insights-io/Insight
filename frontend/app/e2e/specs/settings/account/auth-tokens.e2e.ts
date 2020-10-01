import { get } from 'ky-universal';
import {
  DataResponse,
  OrganizationDTO,
  APIErrorDataResponse,
} from '@insight/types';

import {
  AccountSettingsAuthTokensPage,
  AccountSettingsDetailsPage,
  Sidebar,
  SignUpPage,
} from '../../../pages';

fixture('/settings/account/auth-tokens').page(
  AccountSettingsAuthTokensPage.path
);

test('[AUTH-TOKENS]: As a user I want to create Auth Token, use it to make authenticated API call and then revoke it', async (t) => {
  const { password, email } = SignUpPage.generateRandomCredentials();
  await SignUpPage.signUpAndLogin(t, {
    email,
    password,
    company: 'My super company',
  });

  await t
    .click(Sidebar.banner.trigger)
    .click(Sidebar.banner.menu.account.settings);

  const organizationId = await AccountSettingsDetailsPage.organizationId
    .innerText;

  await t.click(AccountSettingsAuthTokensPage.sidebar.authTokens);
  const initialAuthTokenCount = await AccountSettingsAuthTokensPage.getTokenCount();

  await t
    .expect(initialAuthTokenCount)
    .eql(0, 'Should have no Auth Tokens initially')
    .click(AccountSettingsAuthTokensPage.createNewButton);

  const tokenAccounAfterCreate = await AccountSettingsAuthTokensPage.getTokenCount();
  await t
    .expect(tokenAccounAfterCreate)
    .eql(1, 'Should create a new auth token');

  const authToken = await AccountSettingsAuthTokensPage.getToken(0);

  const organization = await get(
    `http://localhost:8080/v1/organizations/${organizationId}`,
    {
      headers: { Authorization: `Bearer ${authToken}` },
    }
  ).json<DataResponse<OrganizationDTO>>();

  const revokeButton = AccountSettingsAuthTokensPage.revokeButton(0);
  await t
    .expect(organization.data.id)
    .eql(organizationId, 'ID matches')
    .expect(organization.data.name)
    .eql('My super company', 'Name matches')
    .hover(revokeButton)
    .expect(AccountSettingsAuthTokensPage.revokeTooltipText.visible)
    .ok('Revoke tooltip is visible')
    .click(revokeButton)
    .click(AccountSettingsAuthTokensPage.revokeConfirmButton);

  const tokenAccounAfterRevoke = await AccountSettingsAuthTokensPage.getTokenCount();
  await t.expect(tokenAccounAfterRevoke).eql(0, 'Should delete Auth Token');

  const dataResponse = await get(
    `http://localhost:8080/v1/organizations/${organizationId}`,
    {
      headers: { Authorization: `Bearer ${authToken}` },
      throwHttpErrors: false,
    }
  ).json<APIErrorDataResponse>();

  await t.expect(dataResponse).eql(
    {
      error: {
        statusCode: 401,
        reason: 'Unauthorized',
        message: 'Unauthorized',
      },
    },
    'Should throw 401 Unauthorized after Auth Token has been revoked'
  );
});
