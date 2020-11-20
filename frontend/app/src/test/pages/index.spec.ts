import { sandbox } from '@rebrowse/testing';
import { AuthApi, SessionApi } from 'api';
import { COUNT_BY_LOCATION, COUNT_BY_DEVICE } from 'test/data/sessions';
import { REBROWSE_ORGANIZATION_DTO, REBROWSE_ADMIN_DTO } from 'test/data';
import { authenticatedTestCases } from 'test/utils/next';
import { getServerSideProps } from 'pages/index';
import { mockServerSideRequest } from '@rebrowse/next-testing';
import { responsePromise } from 'test/utils/request';

describe('pages/index', () => {
  authenticatedTestCases(getServerSideProps);

  it('Injects correct server side data', async () => {
    sandbox.stub(document, 'cookie').value('SessionId=123');
    const getSessionStub = sandbox.stub(AuthApi.sso.session, 'get').returns(
      responsePromise({
        status: 200,
        data: {
          user: REBROWSE_ADMIN_DTO,
          organization: REBROWSE_ORGANIZATION_DTO,
        },
      })
    );

    const countByLocationStub = sandbox
      .stub(SessionApi, 'countByLocation')
      .resolves(COUNT_BY_LOCATION);

    const countByDeviceStub = sandbox
      .stub(SessionApi, 'countByDeviceClass')
      .resolves(COUNT_BY_DEVICE);

    const { req, res } = mockServerSideRequest();
    const serverSideProps = await getServerSideProps({
      query: {},
      req,
      res,
      resolvedUrl: '/',
    });

    sandbox.assert.calledWithMatch(getSessionStub, '123', {
      baseURL: 'http://localhost:8080',
    });
    sandbox.assert.calledWithMatch(countByLocationStub, {
      baseURL: 'http://localhost:8082',
      headers: { cookie: 'SessionId=123' },
    });
    sandbox.assert.calledWithMatch(countByDeviceStub, {
      baseURL: 'http://localhost:8082',
      headers: { cookie: 'SessionId=123' },
    });
    expect(serverSideProps).toEqual({
      props: {
        user: REBROWSE_ADMIN_DTO,
        organization: REBROWSE_ORGANIZATION_DTO,
        countByLocation: COUNT_BY_LOCATION,
        countByDeviceClass: COUNT_BY_DEVICE,
      },
    });
  });
});
