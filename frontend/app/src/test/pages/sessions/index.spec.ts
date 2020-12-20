import { getPage } from 'next-page-tester';
import { mockEmptySessionsPage, mockSessionsPage } from 'test/mocks';
import {
  screen,
  waitForElementToBeRemoved,
  render,
} from '@testing-library/react';
import { sandbox } from '@rebrowse/testing';
import type { AutoSizerProps } from 'react-virtualized-auto-sizer';
import userEvent from '@testing-library/user-event';
import { SESSIONS_PAGE } from 'shared/constants/routes';
import { REBROWSE_SESSIONS_DTOS } from 'test/data/sessions';

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
        retrieveRecordingSnippetStub,
        countSessionsStub,
      } = mockEmptySessionsPage();

      /* Render */
      const { page } = await getPage({ route });
      render(page);

      /* Assertions */
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
    // FIXME!
    // eslint-disable-next-line jest/no-disabled-tests
    test.skip('As a user I see sessions in a paginated list that works smoothly', async () => {
      /* Mocks */
      document.cookie = 'SessionId=123';
      const {
        listSessionsStub,
        countSessionsStub,
        getDistinctStub,
      } = mockSessionsPage();

      /* Render */
      const { page } = await getPage({ route });
      const { container } = render(page);

      /* Assertions */
      sandbox.assert.calledWithMatch(countSessionsStub, {
        baseURL: 'http://localhost:8082',
        headers: { cookie: 'SessionId=123' },
      });

      sandbox.assert.calledWithMatch(listSessionsStub, {
        baseURL: 'http://localhost:8082',
        headers: { cookie: 'SessionId=123' },
        search: { sortBy: ['-createdAt'], limit: 20 },
      });

      expect((await screen.findAllByText('Mac OS X • Chrome')).length).toEqual(
        9
      );

      expect(
        screen.getAllByText(/Ljubljana, Slovenia - 82.192.62.51 - (.*)/).length
      ).toEqual(9);

      userEvent.click(screen.getByText('0 Filters'));
      userEvent.click(screen.getByText('Filter event by...'));
      userEvent.click(screen.getByText('City'));

      sandbox.assert.calledWithExactly(getDistinctStub, 'location.city');

      /* Search by city */
      const boydton = 'Boydton';
      userEvent.type(
        screen.getByText('Type something').parentElement?.firstChild
          ?.firstChild as HTMLInputElement,
        boydton
      );

      expect((await screen.findAllByText('Mac OS X • Firefox')).length).toEqual(
        9
      );

      expect(
        screen.getAllByText(
          /Boydton, Virginia, United States - 13.77.88.76 - (.*)/
        ).length
      ).toEqual(9);

      sandbox.assert.calledWithExactly(listSessionsStub, {
        search: {
          limit: 20,
          'location.city': `eq:${boydton}`,
          sortBy: ['-createdAt'],
        },
      });
      sandbox.assert.calledWithExactly(countSessionsStub, {
        search: { 'location.city': `eq:${boydton}` },
      });

      /* Search by country */
      userEvent.click(screen.getByText('1 Filters'));
      userEvent.click(
        container.querySelector('svg[title="Plus"]') as SVGElement
      );
      userEvent.click(screen.getByText('Filter event by...'));
      userEvent.click(screen.getByText('Country'));
      sandbox.assert.calledWithExactly(getDistinctStub, 'location.countryName');

      const slovenia = 'Slovenia';
      userEvent.type(
        screen.getByText('Type something').parentElement?.firstChild
          ?.firstChild as HTMLInputElement,
        slovenia
      );

      await waitForElementToBeRemoved(() =>
        screen.getAllByText('Mac OS X • Firefox')
      );

      sandbox.assert.calledWithExactly(listSessionsStub, {
        search: {
          limit: 20,
          'location.city': `eq:${boydton}`,
          'location.countryName': `eq:${slovenia}`,
          sortBy: ['-createdAt'],
        },
      });
      sandbox.assert.calledWithExactly(countSessionsStub, {
        search: {
          'location.city': `eq:${boydton}`,
          'location.countryName': `eq:${slovenia}`,
        },
      });

      /* Clear all filters */
      container.querySelectorAll('svg[title="Delete"]').forEach((element) => {
        userEvent.click(element);
      });

      expect((await screen.findAllByText('Mac OS X • Chrome')).length).toEqual(
        9
      );

      expect(
        screen.getAllByText(/Ljubljana, Slovenia - 82.192.62.51 - (.*)/).length
      ).toEqual(9);

      sandbox.assert.calledWithExactly(listSessionsStub.lastCall, {
        search: {
          limit: 20,
          sortBy: ['-createdAt'],
        },
      });
      sandbox.assert.calledWithExactly(countSessionsStub.lastCall, {
        search: {},
      });
    });

    test('As a user I should be able to see more details about a session', async () => {
      /* Mocks */
      document.cookie = 'SessionId=123';
      const {
        listSessionsStub,
        countSessionsStub,
        retrieveSessionStub,
      } = mockSessionsPage();

      /* Render */
      const { page } = await getPage({ route });
      render(page);

      /* Assertions */
      sandbox.assert.calledWithMatch(countSessionsStub, {
        baseURL: 'http://localhost:8082',
        headers: { cookie: 'SessionId=123' },
      });

      sandbox.assert.calledWithMatch(listSessionsStub, {
        baseURL: 'http://localhost:8082',
        headers: { cookie: 'SessionId=123' },
        search: { sortBy: ['-createdAt'], limit: 20 },
      });

      userEvent.click(
        screen.getAllByText(/Ljubljana, Slovenia - 82.192.62.51 - (.*)/)[0]
      );

      await screen.findByText('Device ID: 123');
      expect(
        screen.getByText(`Session ${REBROWSE_SESSIONS_DTOS[0].id}`)
      ).toBeInTheDocument();

      sandbox.assert.calledWithExactly(
        retrieveSessionStub,
        REBROWSE_SESSIONS_DTOS[0].id
      );
    });
  });
});
