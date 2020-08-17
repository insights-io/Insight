import { IncomingMessage, ServerResponse } from 'http';

import { sandbox } from '@insight/testing';
import { AuthApi, SessionApi } from 'api';
import { COUNT_BY_LOCATION, COUNT_BY_DEVICE } from 'test/data/sessions';
import { INSIGHT_ADMIN } from 'test/data';

import { getServerSideProps } from './index';

const mockRequest = () => {
  const writeHead = sandbox.stub();
  const end = sandbox.stub();
  const res = ({ writeHead, end } as unknown) as ServerResponse;
  const req = {
    url: '/',
    method: 'GET',
  } as IncomingMessage;

  return { req, res, writeHead, end };
};

describe('pages/index', () => {
  it('Injects correct server side data', async () => {
    sandbox.stub(document, 'cookie').value('SessionId=123');
    const getSessionStub = sandbox.stub(AuthApi.sso, 'session').resolves(({
      status: 200,
      json: () => ({ data: INSIGHT_ADMIN }),
    } as unknown) as Response);

    const countByLocationStub = sandbox
      .stub(SessionApi, 'countByLocation')
      .resolves(COUNT_BY_LOCATION);

    const countByDeviceStub = sandbox
      .stub(SessionApi, 'countByDeviceClass')
      .resolves(COUNT_BY_DEVICE);

    const { req, res } = mockRequest();
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

  it('Should redirect to login if no SessionId', async () => {
    const { req, res, writeHead, end } = mockRequest();
    const serverSideProps = await getServerSideProps({ query: {}, req, res });
    sandbox.assert.calledWithExactly(writeHead, 302, {
      Location: '/login?dest=%2F',
    });
    sandbox.assert.calledWithExactly(end);
    expect(serverSideProps).toEqual({ props: {} });
  });

  it('Should redirect to verification if no SessionId but VerificationId', async () => {
    sandbox.stub(document, 'cookie').value('VerificationId=123');
    const { req, res, writeHead, end } = mockRequest();
    const serverSideProps = await getServerSideProps({ query: {}, req, res });

    sandbox.assert.calledWithExactly(writeHead, 302, {
      Location: '/login/verification?dest=%2F',
    });
    sandbox.assert.calledWithExactly(end);
    expect(serverSideProps).toEqual({ props: {} });
  });

  it('Should redirect to login on expired session', async () => {
    sandbox.stub(document, 'cookie').value('SessionId=123');
    const getSessionStub = sandbox.stub(AuthApi.sso, 'session').resolves(({
      status: 204,
      headers: { get: sandbox.stub() },
    } as unknown) as Response);

    const { req, res, writeHead, end } = mockRequest();
    const serverSideProps = await getServerSideProps({ query: {}, req, res });

    sandbox.assert.calledWithExactly(writeHead, 302, {
      Location: '/login?dest=%2F',
      'set-cookie': undefined,
    });
    sandbox.assert.calledWithExactly(end);
    sandbox.assert.calledWithMatch(getSessionStub, '123', {
      baseURL: undefined,
    });
    expect(serverSideProps).toEqual({ props: {} });
  });

  it('Should redirect to verification on expired session and VerificationId', async () => {
    sandbox.stub(document, 'cookie').value('SessionId=123;VerificationId=1234');
    const getSessionStub = sandbox.stub(AuthApi.sso, 'session').resolves(({
      status: 204,
      headers: { get: sandbox.stub() },
    } as unknown) as Response);

    const { req, res, writeHead, end } = mockRequest();
    const serverSideProps = await getServerSideProps({ query: {}, req, res });

    sandbox.assert.calledWithExactly(writeHead, 302, {
      Location: '/login/verification?dest=%2F',
    });
    sandbox.assert.calledWithExactly(end);
    sandbox.assert.calledWithMatch(getSessionStub, '123', {
      baseURL: undefined,
    });
    expect(serverSideProps).toEqual({ props: {} });
  });
});
