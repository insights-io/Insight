import { mockApiError } from '@rebrowse/storybook';
import { sandbox } from '@rebrowse/testing';
import { screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { getPage } from 'next-page-tester';
import { ACCEPT_INVITE_PAGE } from 'shared/constants/routes';
import { match } from 'sinon';
import { ADMIN_TEAM_INVITE_DTO } from '__tests__/data';
import { mockAcceptTeamInvitePage } from '__tests__/mocks';
import { renderPage } from '__tests__/utils';

describe('/accept-invite', () => {
  /* Data */
  const fullName = 'John Doe';
  const password = 'password1234!';
  const { token } = ADMIN_TEAM_INVITE_DTO;
  const route = `${ACCEPT_INVITE_PAGE}?token=${token}`;

  test('As a user I get redirected to / if no token', async () => {
    /* Mocks */
    document.cookie = 'SessionId=123';
    mockAcceptTeamInvitePage(sandbox);

    /* Server */
    const { page } = await getPage({ route: ACCEPT_INVITE_PAGE });

    /* Client */
    renderPage(page);

    await screen.findByText('Page Visits');
  });

  test('As a user I get an error message if team invite does not exist', async () => {
    /* Mocks */
    const { retrieveTeamInviteStub } = mockAcceptTeamInvitePage(sandbox);

    retrieveTeamInviteStub.rejects(
      mockApiError({
        statusCode: 404,
        message: 'Not Found',
        reason: 'Not Found',
      })
    );

    /* Server */
    const { page } = await getPage({ route });

    sandbox.assert.calledWithExactly(retrieveTeamInviteStub, token, {
      headers: { 'uber-trace-id': (match.string as unknown) as string },
    });

    /* Client */
    renderPage(page);

    await screen.findByText(
      'We could not find team invite you were looking for'
    );

    userEvent.click(screen.getByText('Back to login'));
    await screen.findByText('Sign in with Google');
  });

  test('As a user I can accept a team invite', async () => {
    /* Mocks */
    const {
      retrieveTeamInviteStub,
      acceptTeamInviteStub,
    } = mockAcceptTeamInvitePage(sandbox);

    /* Server */
    const { page } = await getPage({ route });

    sandbox.assert.calledWithExactly(retrieveTeamInviteStub, token, {
      headers: { 'uber-trace-id': (match.string as unknown) as string },
    });

    /* Client */
    renderPage(page);

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
