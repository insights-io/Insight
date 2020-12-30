import { mockApiError } from '@rebrowse/storybook';
import { sandbox } from '@rebrowse/testing';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { AuthApi } from 'api';
import { getPage } from 'next-page-tester';
import { ACCEPT_INVITE_PAGE } from 'shared/constants/routes';
import { match } from 'sinon';
import { ADMIN_TEAM_INVITE_DTO } from '__tests__/data';
import { mockIndexPage } from '__tests__/mocks';
import { httpOkResponse } from '__tests__/utils';

describe('/accept-invite', () => {
  /* Data */
  const fullName = 'John Doe';
  const password = 'password1234!';
  const { token } = ADMIN_TEAM_INVITE_DTO;
  const route = `${ACCEPT_INVITE_PAGE}?token=${token}`;

  test('As a user I get redirected to / if no token', async () => {
    /* Mocks */
    document.cookie = 'SessionId=123';
    mockIndexPage(sandbox);

    /* Server */
    const { page } = await getPage({ route: ACCEPT_INVITE_PAGE });

    /* Client */
    render(page);

    await screen.findByText('Page Visits');
  });

  test('As a user I get an error message if team invite does not exist', async () => {
    /* Mocks */
    const retrieveTeamInviteStub = sandbox
      .stub(AuthApi.organization.teamInvite, 'retrieve')
      .rejects(
        mockApiError({
          statusCode: 404,
          message: 'Not Found',
          reason: 'Not Found',
        })
      );

    /* Server */
    const { page } = await getPage({ route });

    sandbox.assert.calledWithExactly(retrieveTeamInviteStub, token, {
      baseURL: 'http://localhost:8080',
      headers: {
        'uber-trace-id': (match.string as unknown) as string,
      },
    });

    /* Client */
    render(page);

    await screen.findByText(
      'We could not find team invite you were looking for'
    );

    userEvent.click(screen.getByText('Back to login'));
    await screen.findByText('Sign in with Google');
  });

  test('As a user I can accept a team invite', async () => {
    /* Mocks */
    mockIndexPage(sandbox);

    const retrieveTeamInviteStub = sandbox
      .stub(AuthApi.organization.teamInvite, 'retrieve')
      .resolves(httpOkResponse(ADMIN_TEAM_INVITE_DTO));

    const acceptTeamInviteStub = sandbox
      .stub(AuthApi.organization.teamInvite, 'accept')
      .callsFake(() => {
        document.cookie = 'SessionId=123';
        return Promise.resolve({ statusCode: 200, headers: new Headers() });
      });

    /* Server */
    const { page } = await getPage({ route });

    sandbox.assert.calledWithExactly(retrieveTeamInviteStub, token, {
      baseURL: 'http://localhost:8080',
      headers: {
        'uber-trace-id': (match.string as unknown) as string,
      },
    });

    /* Client */
    render(page);

    await screen.findByText(
      'User 123 has invited you to join organization 000000 with role Admin.'
    );

    userEvent.type(screen.getByPlaceholderText('Full name'), fullName);
    userEvent.type(screen.getByPlaceholderText('Password'), password);

    userEvent.click(screen.getByText('Continue'));

    await screen.findByText('Page Visits');

    sandbox.assert.calledWithExactly(acceptTeamInviteStub, token, {
      fullName,
      password,
    });
  });
});
