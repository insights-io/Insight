import { sandbox } from '@rebrowse/testing';
import { TimePrecision } from '@rebrowse/types';
import { screen } from '@testing-library/react';
import { getPage } from 'next-page-tester';
import { mockIndexPage } from 'test/mocks';
import { render } from 'test/utils';

describe('/', () => {
  describe('With no data', () => {
    test('Should render empty charts', async () => {
      const {
        retrieveSessionStub,
        countPagesStub,
        countSessionsStub,
      } = mockIndexPage();

      document.cookie = 'SessionId=123';
      const { page } = await getPage({ route: '/' });

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
      expect(screen.getByText('Country distribution')).toBeInTheDocument();
      expect(screen.getByText('Continent distribution')).toBeInTheDocument();

      // Sessions and Page Visits
      expect(screen.getAllByText('No data').length).toEqual(2);
    });
  });
});
