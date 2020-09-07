import { mockApiError } from '@insight/storybook';
import { sandbox } from '@insight/testing';
import { AuthApi } from 'api';
import { getServerSideProps } from 'pages/login/verification';
import { mockServerSideRequest } from 'test/utils/next';

describe('pages/login/verification', () => {
  it('Injects correct server side data', async () => {
    sandbox.stub(document, 'cookie').value('ChallengeId=123');
    const getChallengeStub = sandbox
      .stub(AuthApi.tfa, 'getChallenge')
      .resolves(['totp']);

    const { req, res, writeHead } = mockServerSideRequest();
    const serverSideProps = await getServerSideProps({ query: {}, req, res });

    sandbox.assert.calledWithMatch(getChallengeStub, '123', {
      baseURL: undefined,
    });

    sandbox.assert.notCalled(writeHead);
    expect(serverSideProps).toEqual({ props: { methods: ['totp'] } });
  });

  it('Should redirect to login when missing challengeId cookie', async () => {
    const { req, res, writeHead } = mockServerSideRequest();
    const serverSideProps = await getServerSideProps({ query: {}, req, res });

    sandbox.assert.calledWithExactly(writeHead, 302, {
      Location: '/login?redirect=%2F',
    });

    expect(serverSideProps).toEqual({ props: {} });
  });

  it('Should redirect to login when challenge not found server side', async () => {
    sandbox.stub(document, 'cookie').value('ChallengeId=123');
    const getChallengeStub = sandbox.stub(AuthApi.tfa, 'getChallenge').rejects(
      mockApiError({
        message: 'Not Found',
        reason: 'Not Found',
        statusCode: 404,
      })
    );

    const { req, res, writeHead } = mockServerSideRequest();
    const serverSideProps = await getServerSideProps({ query: {}, req, res });

    sandbox.assert.calledWithMatch(getChallengeStub, '123', {
      baseURL: undefined,
    });
    sandbox.assert.calledWithExactly(writeHead, 302, {
      Location: '/login?redirect=%2F',
    });

    expect(serverSideProps).toEqual({ props: {} });
  });
});
