import { authenticatedTestCases } from 'test/utils/next';
import { getServerSideProps } from 'pages/sessions';
import { AuthApi, SessionApi } from 'api';
import {
  REBROWSE_ADMIN_DTO,
  REBROWSE_ORGANIZATION_DTO,
  REBROWSE_SESSIONS_DTOS,
} from 'test/data';
import { sandbox } from '@rebrowse/testing';
import { mockServerSideRequest } from '@rebrowse/next-testing';
import { responsePromise } from 'test/utils/request';

describe('pages/sessions', () => {
  authenticatedTestCases(getServerSideProps);

  it('Injects correct server side data', async () => {
    document.cookie = 'SessionId=123';
    const getSsoSessionStub = sandbox.stub(AuthApi.sso.session, 'get').returns(
      responsePromise({
        status: 200,
        data: {
          user: REBROWSE_ADMIN_DTO,
          organization: REBROWSE_ORGANIZATION_DTO,
        },
      })
    );

    const getSessionsStub = sandbox
      .stub(SessionApi, 'getSessions')
      .resolves(REBROWSE_SESSIONS_DTOS);

    const getSessionCountStub = sandbox
      .stub(SessionApi, 'count')
      .resolves({ count: REBROWSE_SESSIONS_DTOS.length });

    const { req, res } = mockServerSideRequest();
    const serverSideProps = await getServerSideProps({
      query: {},
      req,
      res,
      resolvedUrl: '/',
    });

    sandbox.assert.calledWithMatch(getSsoSessionStub, '123', {
      baseURL: 'http://localhost:8080',
      headers: {},
    });

    sandbox.assert.calledWithMatch(getSessionsStub, {
      baseURL: 'http://localhost:8082',
      headers: { cookie: 'SessionId=123' },
      search: { limit: 20, sortBy: ['-createdAt'] },
    });

    sandbox.assert.calledWithMatch(getSessionCountStub, {
      baseURL: 'http://localhost:8082',
      headers: { cookie: 'SessionId=123' },
    });

    expect(serverSideProps).toEqual({
      props: {
        sessionCount: REBROWSE_SESSIONS_DTOS.length,
        sessions: REBROWSE_SESSIONS_DTOS,
        user: REBROWSE_ADMIN_DTO,
        organization: REBROWSE_ORGANIZATION_DTO,
      },
    });
  });
});
