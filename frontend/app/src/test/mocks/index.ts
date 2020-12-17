import { sandbox } from '@rebrowse/testing';
import { AuthApi, PagesApi, SessionApi } from 'api';
import { REBROWSE_SESSION_INFO } from 'test/data';
import { responsePromise } from 'test/utils';

export const mockIndexPage = () => {
  const retrieveSessionStub = sandbox.stub(AuthApi.sso.session, 'get').returns(
    responsePromise({
      status: 200,
      data: REBROWSE_SESSION_INFO,
    })
  );

  const retrieveUserStub = sandbox
    .stub(AuthApi.user, 'me')
    .resolves(REBROWSE_SESSION_INFO.user);

  const retrieveOrganizationStub = sandbox
    .stub(AuthApi.organization, 'get')
    .resolves(REBROWSE_SESSION_INFO.organization);

  const countPagesStub = sandbox.stub(PagesApi, 'count').resolves([]);
  const countSessionsStub = sandbox.stub(SessionApi, 'count').resolves([]);

  return {
    retrieveSessionStub,
    retrieveUserStub,
    countPagesStub,
    countSessionsStub,
    retrieveOrganizationStub,
  };
};
