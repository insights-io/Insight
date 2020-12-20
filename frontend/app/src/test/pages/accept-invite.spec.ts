import { mockApiError } from '@rebrowse/storybook';
import { sandbox } from '@rebrowse/testing';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { AuthApi } from 'api';
import { getPage } from 'next-page-tester';
import { ACCEPT_INVITE_PAGE } from 'shared/constants/routes';
import { ADMIN_TEAM_INVITE_DTO } from 'test/data';
import { mockIndexPage } from 'test/mocks';
import { jsonPromise } from 'test/utils';

describe('/accept-invite', () => {
  /* Data */
  const fullName = 'John Doe';
  const password = 'password1234!';
  const { token } = ADMIN_TEAM_INVITE_DTO;
  const route = `${ACCEPT_INVITE_PAGE}?token=${token}`;

  test('As a user I get redirected to / if no token', async () => {
    /* Mocks */
    document.cookie = 'SessionId=123';
    mockIndexPage();

    /* Render */
    const { page } = await getPage({ route: ACCEPT_INVITE_PAGE });
    render(page);

    /* Assertions */
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

    /* Render */
    const { page } = await getPage({ route });
    render(page);

    /* Assertions */
    sandbox.assert.calledWithMatch(retrieveTeamInviteStub, token, {
      baseURL: 'http://localhost:8080',
    });

    await screen.findByText(
      'We could not find team invite you were looking for'
    );

    userEvent.click(screen.getByText('Back to login'));
    await screen.findByText('Sign in with Google');
  });

  test('As a user I can accept a team invite', async () => {
    /* Mocks */
    mockIndexPage();

    const retrieveTeamInviteStub = sandbox
      .stub(AuthApi.organization.teamInvite, 'retrieve')
      .resolves(ADMIN_TEAM_INVITE_DTO);

    const acceptTeamInviteStub = sandbox
      .stub(AuthApi.organization.teamInvite, 'accept')
      .callsFake(() => {
        document.cookie = 'SessionId=123';
        return jsonPromise({ status: 200 });
      });

    /* Render */
    const { page } = await getPage({ route });
    render(page);

    /* Assertions */
    sandbox.assert.calledWithMatch(retrieveTeamInviteStub, token, {
      baseURL: 'http://localhost:8080',
    });

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
