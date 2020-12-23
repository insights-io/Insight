import { TermCondition } from '@rebrowse/sdk';
import { sandbox } from '@rebrowse/testing';
import { screen, render } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { getPage } from 'next-page-tester';
import { AutoSizerProps } from 'react-virtualized-auto-sizer';
import { SESSIONS_PAGE } from 'shared/constants/routes';
import { REBROWSE_SESSIONS_DTOS } from '__tests__/data/sessions';
import { mockSessionPage, mockSessionsPage } from '__tests__/mocks';

jest.mock('react-virtualized-auto-sizer', () => {
  return {
    __esModule: true,
    default: ({ children }: AutoSizerProps) => {
      return children({ width: 1000, height: 1000 });
    },
  };
});

describe('/sessions/[id]', () => {
  /* Data */
  const route = `${SESSIONS_PAGE}/random`;

  test('As a user I should be redirected to /sessions on 404 request', async () => {
    /* Mocks */
    document.cookie = 'SessionId=123';
    const { retrieveSessionStub } = mockSessionsPage(sandbox);

    /* Server */
    const { page } = await getPage({ route });

    sandbox.assert.calledWithMatch(retrieveSessionStub, 'random', {
      baseURL: 'http://localhost:8082',
      headers: { cookie: 'SessionId=123' },
    });

    /* Client */
    render(page);

    await screen.findAllByText('Mac OS X • Chrome');
  });

  test('As a user I should be able to work with Developer tools', async () => {
    /* Mocks */
    document.cookie = 'SessionId=123';
    const { retrieveSessionStub, searchEventsStub } = mockSessionPage(sandbox);
    const [{ id }] = REBROWSE_SESSIONS_DTOS;

    /* Server */
    const { page } = await getPage({ route: `${SESSIONS_PAGE}/${id}` });

    sandbox.assert.calledWithMatch(retrieveSessionStub, id, {
      baseURL: 'http://localhost:8082',
      headers: { cookie: 'SessionId=123' },
    });

    /* Client */
    render(page);

    userEvent.click(screen.getByLabelText('Developer tools'));

    // Console events
    await screen.findByText(
      `Vendors~main.6e71f8501d51c505cf1d.bundle.js:70044 The default hierarchy separators are changing in Storybook 6.0. '|' and '.' will no longer create a hierarchy, but codemods are available. Read more about it in the migration guide: https://github.com/storybookjs/storybook/blob/master/MIGRATION.md`
    );
    expect(screen.getByText('[Fast Refresh] done')).toBeInTheDocument();
    expect(screen.getByText('Something went wrong')).toBeInTheDocument();
    userEvent.click(
      screen.getByText('Uncaught SyntaxError: Unexpected identifier')
    );
    expect(
      screen.getByText('at eval (__playwright_evaluation_script__45:11:47)', {
        exact: false,
      })
    ).toBeInTheDocument();

    sandbox.assert.calledWithExactly(searchEventsStub, id, {
      search: {
        'event.e': [TermCondition.GTE(9), TermCondition.LTE(10)],
        limit: 1000,
      },
    });

    userEvent.click(screen.getByText('Network'));

    // Fetch events
    await screen.findByText(
      'beat?organizationId=000000&sessionId=d1ae54f7-e285-4bbf-bbeb-3bdc0bc7b0ba&deviceId=1978361a-dfae-4801-8d84-89dd6af21740&pageVisitId=032ba89d-0d8b-4f4e-b60f-516f8291e739'
    );

    sandbox.assert.calledWithExactly(searchEventsStub, id, {
      search: { 'event.e': ['eq:11'], limit: 1000 },
    });
  });

  test.only('As a user I can search for "similar sessions" by tags', async () => {
    /* Mocks */
    document.cookie = 'SessionId=123';
    const { retrieveSessionStub } = mockSessionPage(sandbox);
    const [{ id }] = REBROWSE_SESSIONS_DTOS;

    /* Server */
    const { page } = await getPage({ route: `${SESSIONS_PAGE}/${id}` });

    sandbox.assert.calledWithMatch(retrieveSessionStub, id, {
      baseURL: 'http://localhost:8082',
      headers: { cookie: 'SessionId=123' },
    });

    /* Client */
    render(page);

    await screen.findByText(id);
  });
});
