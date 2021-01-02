import { sandbox } from '@rebrowse/testing';
import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { getPage } from 'next-page-tester';
import { INCLUDE_CREDENTIALS } from 'sdk';
import { ORGANIZATION_SETTINGS_AUTH_PAGE } from 'shared/constants/routes';
import { match } from 'sinon';
import { SSO_SAML_SETUP_DTO } from '__tests__/data';
import { mockOrganizationAuthPage } from '__tests__/mocks';
import { renderPage } from '__tests__/utils';

describe('/settings/organization/auth', () => {
  /* Data */
  const route = ORGANIZATION_SETTINGS_AUTH_PAGE;

  test('As a user I can disable existing SSO setup', async () => {
    /* Mocks */
    document.cookie = 'SessionId=123';
    const {
      retrieveSsoSetupStub,
      disableSsoSetupStub,
    } = mockOrganizationAuthPage(sandbox, { ssoSetup: SSO_SAML_SETUP_DTO });

    /* Server */
    const { page } = await getPage({ route });

    sandbox.assert.calledWithExactly(retrieveSsoSetupStub, {
      headers: {
        cookie: 'SessionId=123',
        'uber-trace-id': (match.string as unknown) as string,
      },
    });

    /* Client */
    renderPage(page);

    const oktaToggle = screen
      .getByText('Enable your organization to sign in with Okta.')
      .parentElement?.parentElement?.parentElement?.querySelector(
        'input'
      ) as HTMLInputElement;

    expect(oktaToggle).toBeChecked();
    expect(oktaToggle).toHaveAttribute('type', 'checkbox');
    userEvent.click(oktaToggle);

    expect(screen.getByText('Disable Okta authentication')).toBeInTheDocument();
    userEvent.click(screen.getByText('Disable'));

    await waitFor(() => {
      expect(oktaToggle).not.toBeChecked();
    });

    sandbox.assert.calledWithExactly(disableSsoSetupStub, INCLUDE_CREDENTIALS);
  });

  test('As a user I can enable new SSO setup', async () => {
    /* Mocks */
    document.cookie = 'SessionId=123';
    const {
      retrieveSsoSetupStub,
      createSsoSetupStub,
    } = mockOrganizationAuthPage(sandbox);

    /* Server */
    const { page } = await getPage({ route });

    sandbox.assert.calledWithExactly(retrieveSsoSetupStub, {
      headers: {
        cookie: 'SessionId=123',
        'uber-trace-id': (match.string as unknown) as string,
      },
    });

    /* Client */
    renderPage(page);

    const githubToggle = screen
      .getByText('Enable your organization to sign in with Github.')
      .parentElement?.parentElement?.parentElement?.querySelector(
        'input'
      ) as HTMLInputElement;

    expect(githubToggle).not.toBeChecked();
    expect(githubToggle).toHaveAttribute('type', 'checkbox');
    userEvent.click(githubToggle);

    expect(screen.getByText('Setup Github authentication')).toBeInTheDocument();
    userEvent.click(screen.getByText('Enable'));

    await waitFor(() => {
      expect(githubToggle).toBeChecked();
    });

    sandbox.assert.calledWithExactly(
      createSsoSetupStub,
      'github',
      undefined,
      INCLUDE_CREDENTIALS
    );
  });
});
