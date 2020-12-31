import { sandbox } from '@rebrowse/testing';
import { screen, waitForElementToBeRemoved } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { getPage } from 'next-page-tester';
import { ACCOUNT_SETTINGS_AUTH_TOKENS_PAGE } from 'shared/constants/routes';
import { match } from 'sinon';
import { AUTH_TOKEN_DTO } from '__tests__/data/sso';
import { mockAcocuntSettingsAuthTokensPage } from '__tests__/mocks';
import { renderPage } from '__tests__/utils';

describe('/settings/account/auth-token', () => {
  /* Data */
  const route = ACCOUNT_SETTINGS_AUTH_TOKENS_PAGE;

  test('As a user I can create new auth token', async () => {
    /* Mocks */
    document.cookie = 'SessionId=123';
    const {
      listAuthTokensStub,
      createAuthTokensStub,
    } = mockAcocuntSettingsAuthTokensPage(sandbox);

    /* Server */
    const { page } = await getPage({ route });

    sandbox.assert.calledWithExactly(listAuthTokensStub, {
      baseURL: 'http://localhost:8080',
      headers: {
        cookie: 'SessionId=123',
        'uber-trace-id': (match.string as unknown) as string,
      },
    });

    /* Client */
    renderPage(page);

    expect(screen.getByText(AUTH_TOKEN_DTO.token)).toBeInTheDocument();
    userEvent.click(screen.getByText('Create new'));

    await screen.findByText('Auth token successfully created');

    sandbox.assert.calledWithExactly(createAuthTokensStub);
  });

  test('As a user I can revoke auth token', async () => {
    /* Mocks */
    document.cookie = 'SessionId=123';
    const {
      listAuthTokensStub,
      deleteAuthTokenStub,
    } = mockAcocuntSettingsAuthTokensPage(sandbox);

    /* Server */
    const { page } = await getPage({ route });

    sandbox.assert.calledWithExactly(listAuthTokensStub, {
      baseURL: 'http://localhost:8080',
      headers: {
        cookie: 'SessionId=123',
        'uber-trace-id': (match.string as unknown) as string,
      },
    });

    /* Client */
    const { container } = renderPage(page);

    expect(screen.getByText(AUTH_TOKEN_DTO.token)).toBeInTheDocument();

    userEvent.click(
      container.querySelector('svg[title="Delete"]') as SVGSVGElement
    );

    expect(
      screen.getByText('Are you sure you want to revoke Auth Token?')
    ).toBeInTheDocument();

    userEvent.click(screen.getByText('Yes'));

    await waitForElementToBeRemoved(() =>
      screen.queryByText(AUTH_TOKEN_DTO.token)
    );

    sandbox.assert.calledWithExactly(deleteAuthTokenStub, AUTH_TOKEN_DTO.token);
  });
});
