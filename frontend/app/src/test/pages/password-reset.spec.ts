import { sandbox } from '@rebrowse/testing';
import { AuthApi } from 'api';
import { getServerSideProps } from 'pages/password-reset/index';
import { mockServerSideRequest } from '@rebrowse/next-testing';

describe('pages/password-reset', () => {
  it('Injects correct server side data', async () => {
    const resetExistsStub = sandbox
      .stub(AuthApi.password, 'resetExists')
      .resolves({ data: true });

    const { req, res } = mockServerSideRequest();
    const serverSideProps = await getServerSideProps({
      req,
      res,
      query: { token: '123' },
      resolvedUrl: '/',
    });
    expect(serverSideProps).toEqual({ props: { exists: true, token: '123' } });
    sandbox.assert.calledWithMatch(resetExistsStub, '123', {
      baseURL: 'http://localhost:8080',
      headers: {},
    });
  });

  it('Should handle case when token does not exist server side', async () => {
    const resetExistsStub = sandbox
      .stub(AuthApi.password, 'resetExists')
      .resolves({ data: false });

    const { req, res } = mockServerSideRequest();
    const serverSideProps = await getServerSideProps({
      req,
      res,
      query: { token: '123' },
      resolvedUrl: '/',
    });
    expect(serverSideProps).toEqual({ props: { exists: false } });
    sandbox.assert.calledWithMatch(resetExistsStub, '123', {
      baseURL: 'http://localhost:8080',
      headers: {},
    });
  });

  it('Should handle case with no token', async () => {
    const { req, res } = mockServerSideRequest();
    const serverSideProps = await getServerSideProps({
      req,
      res,
      query: {},
      resolvedUrl: '/',
    });
    expect(serverSideProps).toEqual({ props: { exists: false } });
  });
});
