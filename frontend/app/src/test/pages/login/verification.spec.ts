import { mockApiError } from '@rebrowse/storybook';
import { sandbox } from '@rebrowse/testing';
import { AuthApi } from 'api';
import { getServerSideProps } from 'pages/login/verification';
import { mockServerSideRequest } from '@rebrowse/next-testing';
import { REBROWSE_ADMIN_DTO } from 'test/data';

describe('pages/login/verification', () => {
  it('Injects correct server side data when existing MFA methods', async () => {
    document.cookie = 'ChallengeId=123';
    const getChallengeStub = sandbox
      .stub(AuthApi.mfa.challenge, 'get')
      .resolves(['totp']);

    const { req, res, writeHead } = mockServerSideRequest();
    const serverSideProps = await getServerSideProps({
      query: {},
      req,
      res,
      resolvedUrl: '/',
    });

    sandbox.assert.calledWithMatch(getChallengeStub, '123', {
      baseURL: 'http://localhost:8080',
    });

    sandbox.assert.notCalled(writeHead);
    expect(serverSideProps).toEqual({ props: { methods: ['totp'] } });
  });

  it('Injects correct server side data when no existing MFA methods', async () => {
    document.cookie = 'ChallengeId=123';
    const getChallengeStub = sandbox
      .stub(AuthApi.mfa.challenge, 'get')
      .resolves([]);

    const retrieveUserStub = sandbox
      .stub(AuthApi.mfa.challenge, 'retrieveUser')
      .resolves(REBROWSE_ADMIN_DTO);

    const { req, res, writeHead } = mockServerSideRequest();
    const serverSideProps = await getServerSideProps({
      query: {},
      req,
      res,
      resolvedUrl: '/',
    });

    sandbox.assert.calledWithMatch(getChallengeStub, '123', {
      baseURL: 'http://localhost:8080',
    });

    sandbox.assert.calledWithMatch(retrieveUserStub, '123', {
      baseURL: 'http://localhost:8080',
    });

    sandbox.assert.notCalled(writeHead);
    expect(serverSideProps).toEqual({ props: { user: REBROWSE_ADMIN_DTO } });
  });

  it('Should redirect to login when missing challengeId cookie', async () => {
    const { req, res, writeHead } = mockServerSideRequest();
    const serverSideProps = await getServerSideProps({
      query: {},
      req,
      res,
      resolvedUrl: '/',
    });

    sandbox.assert.calledWithExactly(writeHead, 302, {
      Location: '/login?redirect=%2F',
    });

    expect(serverSideProps).toEqual({ props: {} });
  });

  it('Should redirect to login when challenge not found server side', async () => {
    document.cookie = 'ChallengeId=123';
    const getChallengeStub = sandbox.stub(AuthApi.mfa.challenge, 'get').rejects(
      mockApiError({
        message: 'Not Found',
        reason: 'Not Found',
        statusCode: 404,
      })
    );

    const { req, res, writeHead } = mockServerSideRequest();
    const serverSideProps = await getServerSideProps({
      query: {},
      req,
      res,
      resolvedUrl: '/',
    });

    sandbox.assert.calledWithMatch(getChallengeStub, '123', {
      baseURL: 'http://localhost:8080',
    });
    sandbox.assert.calledWithExactly(writeHead, 302, {
      Location: '/login?redirect=%2F',
    });

    expect(serverSideProps).toEqual({ props: {} });
  });
});
