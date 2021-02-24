import { sandbox, getPage } from '@rebrowse/testing';
import { render, screen } from '@testing-library/react';
import { APP_BASE_URL, ACCOUNTS_BASE_URL } from 'shared/constants';
import { mockLandingPage } from '__tests__/mocks';

describe('/', () => {
  /* Data */
  const route = '/';
  const sessionId = '123';

  test('As a user I should be able to navigate directly to app when logged in', async () => {
    document.cookie = `SessionId=${sessionId}`;
    const { retrieveSsoSessionStub } = mockLandingPage(sandbox);
    const { page } = await getPage({ route });
    sandbox.assert.calledWithExactly(retrieveSsoSessionStub, sessionId);

    /* Client */
    render(page);

    expect(screen.getByText('Go to app').parentElement).toHaveAttribute(
      'href',
      APP_BASE_URL
    );
  });

  test('As a user I should be able to get started when not logged in', async () => {
    const { retrieveSsoSessionStub } = mockLandingPage(sandbox);
    const { page } = await getPage({ route });
    sandbox.assert.notCalled(retrieveSsoSessionStub);

    render(page);

    expect(screen.getByText('Get started').parentElement).toHaveAttribute(
      'href',
      ACCOUNTS_BASE_URL
    );
  });
});
