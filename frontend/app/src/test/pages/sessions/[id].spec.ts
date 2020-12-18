import { sandbox } from '@rebrowse/testing';
import { screen } from '@testing-library/react';
import { getPage } from 'next-page-tester';
import { AutoSizerProps } from 'react-virtualized-auto-sizer';
import { SESSIONS_PAGE } from 'shared/constants/routes';
import { mockSessionsPage } from 'test/mocks';
import { render } from 'test/utils';

jest.mock('react-virtualized-auto-sizer', () => {
  return {
    __esModule: true,
    default: ({ children }: AutoSizerProps) => {
      return children({ width: 1000, height: 1000 });
    },
  };
});

describe('/sessions/[id]', () => {
  test('As a user I should be redirected to /sessions on 404 request', async () => {
    document.cookie = 'SessionId=123';
    const { retrieveSessionStub } = mockSessionsPage();
    const { page } = await getPage({ route: `${SESSIONS_PAGE}/random` });
    render(page);

    sandbox.assert.calledWithMatch(retrieveSessionStub, 'random', {
      baseURL: 'http://localhost:8082',
      headers: { cookie: 'SessionId=123' },
    });

    // SSR redirect to /sessions page
    // sesions fitting into the virtualized list height
    expect((await screen.findAllByText('Mac OS X • Chrome')).length).toEqual(
      16
    );
  });
});
