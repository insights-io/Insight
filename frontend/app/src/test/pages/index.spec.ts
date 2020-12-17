import { sandbox } from '@rebrowse/testing';
import { TimePrecision } from '@rebrowse/types';
import { screen } from '@testing-library/react';
import { AuthApi, PagesApi, SessionApi } from 'api';
import { getPage } from 'next-page-tester';
import { REBROWSE_SESSION_INFO } from 'test/data';
import { responsePromise, render } from 'test/utils';

const retrieveUserStub = sandbox.stub(AuthApi.sso.session, 'get').returns(
  responsePromise({
    status: 200,
    data: REBROWSE_SESSION_INFO,
  })
);

describe('/', () => {
  describe('With no data', () => {
    const countPagesStub = sandbox.stub(PagesApi, 'count').resolves([]);
    const countSessionsStub = sandbox.stub(SessionApi, 'count').resolves([]);

    test('Should render empty charts', async () => {
      document.cookie = 'SessionId=123';
      const { page } = await getPage({ route: '/' });

      sandbox.assert.calledWithMatch(retrieveUserStub, '123', {
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
