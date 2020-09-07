/* eslint-disable jest/no-export */
import { IncomingMessage, ServerResponse } from 'http';

import { sandbox } from '@insight/testing';
import { GetServerSideProps } from 'next';
import { AuthApi } from 'api';

export const mockServerSideRequest = () => {
  const writeHead = sandbox.stub();
  const end = sandbox.stub();
  const res = ({ writeHead, end } as unknown) as ServerResponse;
  const req = {
    url: '/',
    method: 'GET',
  } as IncomingMessage;

  return { req, res, writeHead, end };
};

export function authenticatedTestCases<T>(
  getServerSideProps: GetServerSideProps<T>
) {
  return [
    it('Should redirect to login if no SessionId', async () => {
      const { req, res, writeHead, end } = mockServerSideRequest();
      const serverSideProps = await getServerSideProps({ query: {}, req, res });
      sandbox.assert.calledWithExactly(writeHead, 302, {
        Location: '/login?redirect=%2F',
      });
      sandbox.assert.calledWithExactly(end);
      expect(serverSideProps).toEqual({ props: {} });
    }),

    it('Should redirect to verification if no SessionId but ChallengeId', async () => {
      sandbox.stub(document, 'cookie').value('ChallengeId=123');
      const { req, res, writeHead, end } = mockServerSideRequest();
      const serverSideProps = await getServerSideProps({ query: {}, req, res });

      sandbox.assert.calledWithExactly(writeHead, 302, {
        Location: '/login/verification?redirect=%2F',
      });
      sandbox.assert.calledWithExactly(end);
      expect(serverSideProps).toEqual({ props: {} });
    }),

    it('Should redirect to login on expired session', async () => {
      sandbox.stub(document, 'cookie').value('SessionId=123');
      const getSessionStub = sandbox.stub(AuthApi.sso, 'session').resolves(({
        status: 204,
        headers: { get: sandbox.stub() },
      } as unknown) as Response);

      const { req, res, writeHead, end } = mockServerSideRequest();
      const serverSideProps = await getServerSideProps({ query: {}, req, res });

      sandbox.assert.calledWithExactly(writeHead, 302, {
        Location: '/login?redirect=%2F',
        'set-cookie': undefined,
      });
      sandbox.assert.calledWithExactly(end);
      sandbox.assert.calledWithMatch(getSessionStub, '123', {
        baseURL: undefined,
      });
      expect(serverSideProps).toEqual({ props: {} });
    }),

    it('Should redirect to verification on expired session and ChallengeId', async () => {
      sandbox.stub(document, 'cookie').value('SessionId=123;ChallengeId=1234');
      const getSessionStub = sandbox.stub(AuthApi.sso, 'session').resolves(({
        status: 204,
        headers: { get: sandbox.stub() },
      } as unknown) as Response);

      const { req, res, writeHead, end } = mockServerSideRequest();
      const serverSideProps = await getServerSideProps({ query: {}, req, res });

      sandbox.assert.calledWithExactly(writeHead, 302, {
        Location: '/login/verification?redirect=%2F',
      });
      sandbox.assert.calledWithExactly(end);
      sandbox.assert.calledWithMatch(getSessionStub, '123', {
        baseURL: undefined,
      });
      expect(serverSideProps).toEqual({ props: {} });
    }),
  ];
}
