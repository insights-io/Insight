import { getPage } from 'next-page-tester';
import { mockEmptySessionsPage, mockSessionsPage } from 'test/mocks';
import { render } from 'test/utils';
import { screen } from '@testing-library/react';
import { sandbox } from '@rebrowse/testing';
import type { AutoSizerProps } from 'react-virtualized-auto-sizer';

jest.mock('react-virtualized-auto-sizer', () => {
  return {
    __esModule: true,
    default: ({ children }: AutoSizerProps) => {
      return children({ width: 1000, height: 1000 });
    },
  };
});

describe('/sessions', () => {
  describe('With no sessions', () => {
    test('As a user I should see a bootstrap script when no sessions has been tracked yet', async () => {
      const {
        listSessionsStub,
        retrieveRecordingSnippetStub,
        countSessionsStub,
      } = mockEmptySessionsPage();

      const { page } = await getPage({ route: '/sessions' });
      render(page);

      await screen.findByText(
        'Ready to get insights? Setup the recording snippet.'
      );

      sandbox.assert.calledWithExactly(
        retrieveRecordingSnippetStub,
        'https://static.rebrowse.dev/b/rebrowse.js'
      );

      sandbox.assert.calledWithMatch(countSessionsStub, {
        baseURL: 'http://localhost:8082',
        headers: { cookie: 'SessionId=123' },
      });

      sandbox.assert.calledWithMatch(listSessionsStub, {
        baseURL: 'http://localhost:8082',
        headers: { cookie: 'SessionId=123' },
        search: { sortBy: ['-createdAt'], limit: 20 },
      });

      expect(
        screen.getByText(`._i_org = '000000';`, { exact: false })
      ).toBeInTheDocument();
      expect(
        screen.getByText(
          `.src = 'https://static.rebrowse.dev/s/rebrowse.js';`,
          { exact: false }
        )
      ).toBeInTheDocument();
    });
  });

  describe('With many sessions', () => {
    test('As a user I see sessions in a paginated list that works smoothly', async () => {
      const { listSessionsStub, countSessionsStub } = mockSessionsPage();
      const { page } = await getPage({ route: '/sessions' });
      render(page);

      sandbox.assert.calledWithMatch(countSessionsStub, {
        baseURL: 'http://localhost:8082',
        headers: { cookie: 'SessionId=123' },
      });

      sandbox.assert.calledWithMatch(listSessionsStub, {
        baseURL: 'http://localhost:8082',
        headers: { cookie: 'SessionId=123' },
        search: { sortBy: ['-createdAt'], limit: 20 },
      });

      // sesions fitting into the virtualized list height
      expect((await screen.findAllByText('Mac OS X • Chrome')).length).toEqual(
        16
      );

      expect(
        screen.getAllByText(/Ljubljana, Slovenia - 82.192.62.51 - (.*)/).length
      ).toEqual(16);

      // TODO: make tests more comprehensive -- add filtering, scrolling?
    });
  });
});
