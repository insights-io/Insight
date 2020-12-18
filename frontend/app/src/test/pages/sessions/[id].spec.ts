import { mockApiError } from '@rebrowse/storybook';
import { sandbox } from '@rebrowse/testing';
import { screen } from '@testing-library/react';
import { SessionApi } from 'api';
import { getPage } from 'next-page-tester';
import { AutoSizerProps } from 'react-virtualized-auto-sizer';
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
    mockSessionsPage();

    const retrieveSessionStub = sandbox.stub(SessionApi, 'getSession').rejects(
      mockApiError({
        message: 'Not Found',
        reason: 'Not Found',
        statusCode: 404,
      })
    );

    const { page } = await getPage({ route: '/sessions/random' });
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
