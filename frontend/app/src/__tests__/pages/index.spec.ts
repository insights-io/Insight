import { sandbox } from '@rebrowse/testing';
import { TimePrecision } from '@rebrowse/types';
import { screen } from '@testing-library/react';
import { getPage } from 'next-page-tester';
import { INDEX_PAGE } from 'shared/constants/routes';
import { match } from 'sinon';
import { mockIndexPage } from '__tests__/mocks';
import { renderPage } from '__tests__/utils';

describe('/', () => {
  /* Data */
  const route = INDEX_PAGE;

  test('Should render empty charts when no data', async () => {
    /* Mocks */
    document.cookie = 'SessionId=123';
    const {
      retrieveSessionStub,
      countPageVisitsStub,
      countSessionsStub,
    } = mockIndexPage(sandbox, { sessions: [] });

    /* Server */
    const { page } = await getPage({ route });

    sandbox.assert.calledWithExactly(retrieveSessionStub, '123', {
      headers: { 'uber-trace-id': (match.string as unknown) as string },
    });

    sandbox.assert.calledWithExactly(countPageVisitsStub, {
      headers: {
        cookie: 'SessionId=123',
        'uber-trace-id': (match.string as unknown) as string,
      },
      search: {
        createdAt: (match.string as unknown) as string,
        dateTrunc: TimePrecision.DAY,
        groupBy: ['createdAt'],
      },
    });

    sandbox.assert.calledWithExactly(countSessionsStub.firstCall, {
      headers: {
        cookie: 'SessionId=123',
        'uber-trace-id': (match.string as unknown) as string,
      },
      search: {
        createdAt: (match.string as unknown) as string,
        dateTrunc: TimePrecision.DAY,
        groupBy: ['createdAt'],
      },
    });

    sandbox.assert.calledWithExactly(countSessionsStub.secondCall, {
      headers: { cookie: 'SessionId=123', 'uber-trace-id': match.string },
      search: {
        createdAt: (match.string as unknown) as string,
        groupBy: ['userAgent.deviceClass'],
      },
    });

    sandbox.assert.calledWithExactly(countSessionsStub.thirdCall, {
      headers: { cookie: 'SessionId=123', 'uber-trace-id': match.string },
      search: {
        createdAt: (match.string as unknown) as string,
        groupBy: ['location.countryName', 'location.continentName'],
      },
    });

    /* Client */
    renderPage(page);

    expect(await screen.findByText('Page Visits')).toBeInTheDocument();
    expect(screen.getByText('Sessions')).toBeInTheDocument();
    expect(screen.getByText('Country Breakdown')).toBeInTheDocument();
    expect(screen.getByText('Continent Breakdown')).toBeInTheDocument();
    expect(screen.getByText('Device Breakdown')).toBeInTheDocument();

    // Sessions and Page Visits
    expect(screen.getAllByText('No data').length).toEqual(2);
  });
});
