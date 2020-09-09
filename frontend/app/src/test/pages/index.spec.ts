import { sandbox } from '@insight/testing';
import { AuthApi, SessionApi } from 'api';
import { COUNT_BY_LOCATION, COUNT_BY_DEVICE } from 'test/data/sessions';
import { INSIGHT_ADMIN } from 'test/data';
import { mockServerSideRequest, authenticatedTestCases } from 'test/utils/next';
import { getServerSideProps } from 'pages/index';

describe('pages/index', () => {
  authenticatedTestCases(getServerSideProps);

  it('Injects correct server side data', async () => {
    sandbox.stub(document, 'cookie').value('SessionId=123');
    const getSessionStub = sandbox.stub(AuthApi.sso.session, 'get').resolves(({
      status: 200,
      json: () => ({ data: INSIGHT_ADMIN }),
    } as unknown) as Response);

    const countByLocationStub = sandbox
      .stub(SessionApi, 'countByLocation')
      .resolves(COUNT_BY_LOCATION);

    const countByDeviceStub = sandbox
      .stub(SessionApi, 'countByDeviceClass')
      .resolves(COUNT_BY_DEVICE);

    const { req, res } = mockServerSideRequest();
    const serverSideProps = await getServerSideProps({ query: {}, req, res });

    sandbox.assert.calledWithMatch(getSessionStub, '123', {
      baseURL: undefined,
    });
    sandbox.assert.calledWithMatch(countByLocationStub, {
      baseURL: undefined,
      headers: { cookie: 'SessionId=123' },
    });
    sandbox.assert.calledWithMatch(countByDeviceStub, {
      baseURL: undefined,
      headers: { cookie: 'SessionId=123' },
    });
    expect(serverSideProps).toEqual({
      props: {
        user: INSIGHT_ADMIN,
        countByLocation: COUNT_BY_LOCATION,
        countByDeviceClass: COUNT_BY_DEVICE,
      },
    });
  });
});
