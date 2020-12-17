import { sandbox } from '@rebrowse/testing';
import { GetServerSideProps } from 'next';
import { AuthApi } from 'api';
import { mockServerSideRequest } from '@rebrowse/next-testing';

// eslint-disable-next-line jest/no-export
export function authenticatedTestCases<T>(
  getServerSideProps: GetServerSideProps<T>
) {
  return [
    it('Should redirect to login if no SessionId', async () => {
      const { req, res, writeHead, end } = mockServerSideRequest();
      const serverSideProps = await getServerSideProps({
        query: {},
        req,
        res,
        resolvedUrl: '/',
      });
      sandbox.assert.calledWithExactly(writeHead, 302, {
        Location: '/login?redirect=%2F',
      });
      sandbox.assert.calledWithExactly(end);
      expect(serverSideProps).toEqual({ props: {} });
    }),

    it('Should redirect to verification if no SessionId but ChallengeId', async () => {
      document.cookie = 'ChallengeId=123';
      const { req, res, writeHead, end } = mockServerSideRequest();
      const serverSideProps = await getServerSideProps({
        query: {},
        req,
        res,
        resolvedUrl: '/',
      });

      sandbox.assert.calledWithExactly(writeHead, 302, {
        Location: '/login/verification?redirect=%2F',
      });
      sandbox.assert.calledWithExactly(end);
      expect(serverSideProps).toEqual({ props: {} });
    }),

    it('Should redirect to login on expired session', async () => {
      document.cookie = 'SessionId=123';
      const getSessionStub = sandbox
        .stub(AuthApi.sso.session, 'get')
        .resolves(({
          status: 204,
          headers: { get: sandbox.stub() },
        } as unknown) as Response);

      const { req, res, writeHead, end } = mockServerSideRequest();
      const serverSideProps = await getServerSideProps({
        query: {},
        req,
        res,
        resolvedUrl: '/',
      });

      sandbox.assert.calledWithExactly(writeHead, 302, {
        Location: '/login?redirect=%2F',
        'set-cookie': undefined,
      });
      sandbox.assert.calledWithExactly(end);
      sandbox.assert.calledWithMatch(getSessionStub, '123', {
        baseURL: 'http://localhost:8080',
      });
      expect(serverSideProps).toEqual({ props: {} });
    }),

    it('Should redirect to verification on expired session and ChallengeId', async () => {
      document.cookie = 'SessionId=123';
      document.cookie = 'ChallengeId=1234';

      const getSessionStub = sandbox
        .stub(AuthApi.sso.session, 'get')
        .resolves(({
          status: 204,
          headers: { get: sandbox.stub() },
        } as unknown) as Response);

      const { req, res, writeHead, end } = mockServerSideRequest();
      const serverSideProps = await getServerSideProps({
        query: {},
        req,
        res,
        resolvedUrl: '/',
      });

      sandbox.assert.calledWithExactly(writeHead, 302, {
        Location: '/login/verification?redirect=%2F',
      });
      sandbox.assert.calledWithExactly(end);
      sandbox.assert.calledWithMatch(getSessionStub, '123', {
        baseURL: 'http://localhost:8080',
      });
      expect(serverSideProps).toEqual({ props: {} });
    }),
  ];
}
