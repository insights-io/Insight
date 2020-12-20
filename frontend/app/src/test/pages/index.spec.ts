import { sandbox } from '@rebrowse/testing';
import { TimePrecision } from '@rebrowse/types';
import { render, screen } from '@testing-library/react';
import { getPage } from 'next-page-tester';
import { INDEX_PAGE } from 'shared/constants/routes';
import { mockIndexPage } from 'test/mocks';

describe('/', () => {
  /* Data */
  const route = INDEX_PAGE;

  describe('With no data', () => {
    test('Should render empty charts', async () => {
      /* Mocks */
      document.cookie = 'SessionId=123';
      const {
        retrieveSessionStub,
        countPagesStub,
        countSessionsStub,
      } = mockIndexPage();

      /* Render */
      const { page } = await getPage({ route });

      /* Assertions */
      sandbox.assert.calledWithMatch(retrieveSessionStub, '123', {
        baseURL: 'http://localhost:8080',
      });

      sandbox.assert.calledWithMatch(countPagesStub, {
        baseURL: 'http://localhost:8082',
        headers: { cookie: 'SessionId=123' },
        search: { dateTrunc: TimePrecision.DAY, groupBy: ['createdAt'] },
      });

      sandbox.assert.calledWithMatch(countSessionsStub.firstCall, {
        baseURL: 'http://localhost:8082',
        headers: { cookie: 'SessionId=123' },
        search: { dateTrunc: TimePrecision.DAY, groupBy: ['createdAt'] },
      });

      sandbox.assert.calledWithMatch(countSessionsStub.secondCall, {
        baseURL: 'http://localhost:8082',
        headers: { cookie: 'SessionId=123' },
        search: {
          groupBy: ['userAgent.deviceClass'],
        },
      });

      sandbox.assert.calledWithMatch(countSessionsStub.thirdCall, {
        baseURL: 'http://localhost:8082',
        headers: { cookie: 'SessionId=123' },
        search: {
          groupBy: ['location.countryName', 'location.continentName'],
        },
      });

      render(page);

      expect(await screen.findByText('Page Visits')).toBeInTheDocument();
      expect(screen.getByText('Sessions')).toBeInTheDocument();
      expect(screen.getByText('Country Breakdown')).toBeInTheDocument();
      expect(screen.getByText('Continent Breakdown')).toBeInTheDocument();
      expect(screen.getByText('Device Breakdown')).toBeInTheDocument();

      // Sessions and Page Visits
      expect(screen.getAllByText('No data').length).toEqual(2);
    });
  });
});
