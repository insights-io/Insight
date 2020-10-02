import { authenticatedTestCases } from 'test/utils/next';
import { getServerSideProps } from 'pages/sessions';
import { AuthApi, SessionApi } from 'api';
import { INSIGHT_ADMIN_DTO, INSIGHT_SESSIONS_DTOS } from 'test/data';
import { sandbox } from '@insight/testing';
import { mockServerSideRequest } from '@insight/next-testing';
import { responsePromise } from 'test/utils/request';
import { INSIGHT_ORGANIZATION_DTO } from 'test/data/organization';

describe('pages/sessions', () => {
  authenticatedTestCases(getServerSideProps);

  it('Injects correct server side data', async () => {
    sandbox.stub(document, 'cookie').value('SessionId=123');
    const getSsoSessionStub = sandbox.stub(AuthApi.sso.session, 'get').returns(
      responsePromise({
        status: 200,
        data: {
          user: INSIGHT_ADMIN_DTO,
          organization: INSIGHT_ORGANIZATION_DTO,
        },
      })
    );

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
        user: INSIGHT_ADMIN_DTO,
        organization: INSIGHT_ORGANIZATION_DTO,
      },
    });
  });
});
