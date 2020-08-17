import { mockServerSideRequest } from 'test/utils/next';
import { sandbox } from '@insight/testing';
import { AuthApi } from 'api';

import { getServerSideProps } from './index';

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
    });
    expect(serverSideProps).toEqual({ props: { exists: true, token: '123' } });
    sandbox.assert.calledWithMatch(resetExistsStub, '123', {
      baseURL: undefined,
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
    });
    expect(serverSideProps).toEqual({ props: { exists: false } });
    sandbox.assert.calledWithMatch(resetExistsStub, '123', {
      baseURL: undefined,
      headers: {},
    });
  });

  it('Should handle case with no token', async () => {
    const { req, res } = mockServerSideRequest();
    const serverSideProps = await getServerSideProps({ req, res, query: {} });
    expect(serverSideProps).toEqual({ props: { exists: false } });
  });
});
