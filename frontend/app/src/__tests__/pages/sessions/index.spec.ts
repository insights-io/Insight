import { getPage } from 'next-page-tester';
import { mockEmptySessionsPage, mockSessionsPage } from '__tests__/mocks';
import { screen, waitFor } from '@testing-library/react';
import { sandbox } from '@rebrowse/testing';
import type { AutoSizerProps } from 'react-virtualized-auto-sizer';
import userEvent from '@testing-library/user-event';
import { SESSIONS_PAGE } from 'shared/constants/routes';
import {
  REBROWSE_SESSIONS,
  REBROWSE_SESSIONS_DTOS,
} from '__tests__/data/sessions';
import { sessionDescription } from 'sessions/utils';
import { TermCondition } from '@rebrowse/sdk';
import { match } from 'sinon';
import { renderPage } from '__tests__/utils';
import { INCLUDE_CREDENTIALS } from 'sdk';

jest.mock('react-virtualized-auto-sizer', () => {
  return {
    __esModule: true,
    default: ({ children }: AutoSizerProps) => {
      return children({ width: 500, height: 500 });
    },
  };
});

describe('/sessions', () => {
  /* Data */
  const route = SESSIONS_PAGE;

  describe('With no sessions', () => {
    test('As a user I should see a bootstrap script when no sessions has been tracked yet', async () => {
      /* Mocks */
      document.cookie = 'SessionId=123';
      const {
        listSessionsStub,
        retrieveBoostrapScriptStub,
        countSessionsStub,
      } = mockEmptySessionsPage(sandbox);

      /* Server */
      const { page } = await getPage({ route });

      /* Client */
      renderPage(page);

      await screen.findByText(
        'Ready to get insights? Setup the recording snippet.'
      );

      sandbox.assert.calledWithExactly(
        retrieveBoostrapScriptStub,
        'https://static.rebrowse.dev/b/rebrowse.js'
      );

      sandbox.assert.calledWithExactly(countSessionsStub, {
        headers: {
          cookie: 'SessionId=123',
          'uber-trace-id': (match.string as unknown) as string,
        },
        search: {},
      });

      sandbox.assert.calledWithExactly(listSessionsStub, {
        headers: {
          cookie: 'SessionId=123',
          'uber-trace-id': (match.string as unknown) as string,
        },
        search: { sortBy: ['-createdAt'], limit: 20 },
      });

      expect(
        screen.getByText(`._i_org = '000000';`, { exact: false })
      ).toBeInTheDocument();
      expect(
        screen.getByText(
          `src = 'https://static.rebrowse.dev/s/localhost.rebrowse.js';`,
          { exact: false }
        )
      ).toBeInTheDocument();
    });
  });

  describe('With many sessions', () => {
    test('As a user I see sessions in a paginated list that works smoothly', async () => {
      /* Mocks */
      document.cookie = 'SessionId=123';
      const {
        listSessionsStub,
        countSessionsStub,
        getDistinctSessionsStub,
      } = mockSessionsPage(sandbox);

      /* Server */
      const { page } = await getPage({ route });

      sandbox.assert.calledWithExactly(countSessionsStub, {
        headers: {
          cookie: 'SessionId=123',
          'uber-trace-id': (match.string as unknown) as string,
        },
        search: {},
      });

      sandbox.assert.calledWithExactly(listSessionsStub, {
        headers: {
          cookie: 'SessionId=123',
          'uber-trace-id': (match.string as unknown) as string,
        },
        search: { sortBy: ['-createdAt'], limit: 20 },
      });

      /* Client */
      const { container } = renderPage(page);
      await screen.findAllByText('Mac OS X • Chrome');
      screen.getAllByText(/Ljubljana, Slovenia - 82.192.62.51 - (.*)/);

      userEvent.click(screen.getByText('0 Filters'));
      userEvent.click(screen.getByText('Filter event by...'));
      userEvent.click(screen.getByText('City'));

      sandbox.assert.calledWithExactly(
        getDistinctSessionsStub,
        'location.city'
      );

      /* Search by city */
      const boydton = 'Boydton';
      userEvent.type(
        screen.getByText('Type something').parentElement?.firstChild
          ?.firstChild as HTMLInputElement,
        boydton
      );

      await waitFor(() => {
        sandbox.assert.calledWithExactly(listSessionsStub, {
          search: {
            limit: 20,
            'location.city': TermCondition.EQ(boydton),
            sortBy: ['-createdAt'],
          },
          ...INCLUDE_CREDENTIALS,
        });
        sandbox.assert.calledWithExactly(countSessionsStub, {
          search: { 'location.city': TermCondition.EQ(boydton) },
          ...INCLUDE_CREDENTIALS,
        });
      });

      /* Search by country */
      userEvent.click(screen.getByText('1 Filters'));
      userEvent.click(
        container.querySelector('svg[title="Plus"]') as SVGElement
      );
      userEvent.click(screen.getByText('Filter event by...'));
      userEvent.click(screen.getByText('Country'));
      sandbox.assert.calledWithExactly(
        getDistinctSessionsStub,
        'location.country_name'
      );

      const slovenia = 'Slovenia';
      userEvent.type(
        screen.getByText('Type something').parentElement?.firstChild
          ?.firstChild as HTMLInputElement,
        slovenia
      );

      await waitFor(() => {
        sandbox.assert.calledWithExactly(listSessionsStub, {
          search: {
            limit: 20,
            'location.city': TermCondition.EQ(boydton),
            'location.country_name': TermCondition.EQ(slovenia),
            sortBy: ['-createdAt'],
          },
          ...INCLUDE_CREDENTIALS,
        });
        sandbox.assert.calledWithExactly(countSessionsStub, {
          search: {
            'location.city': TermCondition.EQ(boydton),
            'location.country_name': TermCondition.EQ(slovenia),
          },
          ...INCLUDE_CREDENTIALS,
        });
      });

      /* Clear all filters */
      container.querySelectorAll('svg[title="Delete"]').forEach((element) => {
        userEvent.click(element);
      });

      await waitFor(() => {
        sandbox.assert.calledWithExactly(listSessionsStub.lastCall, {
          search: { limit: 20, sortBy: ['-createdAt'] },
          ...INCLUDE_CREDENTIALS,
        });
        sandbox.assert.calledWithExactly(countSessionsStub.lastCall, {
          search: {},
          ...INCLUDE_CREDENTIALS,
        });
      });
    });

    test('As a user I should be able to see more details about a session', async () => {
      /* Mocks */
      document.cookie = 'SessionId=123';
      const {
        listSessionsStub,
        countSessionsStub,
        retrieveSessionStub,
      } = mockSessionsPage(sandbox);

      /* Server */
      const { page } = await getPage({ route });

      sandbox.assert.calledWithExactly(countSessionsStub, {
        headers: {
          cookie: 'SessionId=123',
          'uber-trace-id': (match.string as unknown) as string,
        },
        search: {},
      });

      sandbox.assert.calledWithExactly(listSessionsStub, {
        headers: {
          cookie: 'SessionId=123',
          'uber-trace-id': (match.string as unknown) as string,
        },
        search: { sortBy: ['-createdAt'], limit: 20 },
      });

      /* Client */
      renderPage(page);

      userEvent.click(
        screen.getAllByText(sessionDescription(REBROWSE_SESSIONS[0]))[0]
      );

      await screen.findByText(REBROWSE_SESSIONS[0].id);

      /* SSR */
      sandbox.assert.calledWithExactly(
        retrieveSessionStub,
        REBROWSE_SESSIONS_DTOS[0].id,
        INCLUDE_CREDENTIALS
      );
    });
  });
});
