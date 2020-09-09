import { authenticatedTestCases, mockServerSideRequest } from 'test/utils/next';
import { getServerSideProps } from 'pages/sessions';
import { AuthApi, SessionApi } from 'api';
import { INSIGHT_ADMIN, INSIGHT_SESSIONS_DTOS } from 'test/data';
import { sandbox } from '@insight/testing';

describe('pages/sessions', () => {
  authenticatedTestCases(getServerSideProps);

  it('Injects correct server side data', async () => {
    sandbox.stub(document, 'cookie').value('SessionId=123');
    const getSsoSessionStub = sandbox
      .stub(AuthApi.sso.session, 'get')
      .resolves(({
        status: 200,
        json: () => ({ data: INSIGHT_ADMIN }),
      } as unknown) as Response);

    const getSessionsStub = sandbox
      .stub(SessionApi, 'getSessions')
      .resolves(INSIGHT_SESSIONS_DTOS);

    const getSessionCountStub = sandbox
      .stub(SessionApi, 'count')
      .resolves({ count: INSIGHT_SESSIONS_DTOS.length });

    const { req, res } = mockServerSideRequest();
    const serverSideProps = await getServerSideProps({ query: {}, req, res });

    sandbox.assert.calledWithMatch(getSsoSessionStub, '123', {
      baseURL: undefined,
      headers: {},
    });

    sandbox.assert.calledWithMatch(getSessionsStub, {
      baseURL: undefined,
      headers: { cookie: 'SessionId=123' },
      search: { limit: 20, sort_by: ['-created_at'] },
    });

    sandbox.assert.calledWithMatch(getSessionCountStub, {
      baseURL: undefined,
      headers: { cookie: 'SessionId=123' },
    });

    expect(serverSideProps).toEqual({
      props: {
        sessionCount: INSIGHT_SESSIONS_DTOS.length,
        sessions: INSIGHT_SESSIONS_DTOS,
        user: INSIGHT_ADMIN,
      },
    });
  });
});
