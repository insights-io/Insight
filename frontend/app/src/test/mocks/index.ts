import { sandbox } from '@rebrowse/testing';
import { AuthApi, PagesApi, SessionApi } from 'api';
import { REBROWSE_SESSIONS_DTOS, REBROWSE_SESSION_INFO } from 'test/data';
import { jsonPromise, textPromise } from 'test/utils';
import ky from 'ky-universal';

export const mockAuth = () => {
  const retrieveSessionStub = sandbox.stub(AuthApi.sso.session, 'get').returns(
    jsonPromise({
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

  return { retrieveSessionStub, retrieveUserStub, retrieveOrganizationStub };
};

export const mockSessionsPage = () => {
  const authMocks = mockAuth();

  const countSessionsStub = sandbox
    .stub(SessionApi, 'count')
    .resolves({ count: REBROWSE_SESSIONS_DTOS.length });

  const listSessionsStub = sandbox
    .stub(SessionApi, 'getSessions')
    .resolves(REBROWSE_SESSIONS_DTOS);

  return { ...authMocks, listSessionsStub, countSessionsStub };
};

export const mockEmptySessionsPage = () => {
  const authMocks = mockAuth();
  const countSessionsStub = sandbox.stub(SessionApi, 'count').resolves([]);
  const listSessionsStub = sandbox.stub(SessionApi, 'getSessions').resolves([]);

  const retrieveRecordingSnippetStub = sandbox.stub(ky, 'get').returns(
    textPromise({
      status: 200,
      data: `((e, s, r) => {
    e._i_debug = !1;
    e._i_host = 'rebrowse.dev';
    e._i_org = '<ORG>';
    e._i_ns = 'IS';
    const t = s.createElement(r);
    t.async = true;
    t.crossOrigin = 'anonymous';
    t.src = 'https://static.rebrowse.dev/s/rebrowse.js';
    const o = s.getElementsByTagName(r)[0];
    o.parentNode.insertBefore(t, o);
  })(window, document, 'script');`,
    })
  );

  return {
    ...authMocks,
    listSessionsStub,
    countSessionsStub,
    retrieveRecordingSnippetStub,
  };
};

export const mockIndexPage = () => {
  const authMocks = mockAuth();
  const countPagesStub = sandbox.stub(PagesApi, 'count').resolves([]);
  const countSessionsStub = sandbox.stub(SessionApi, 'count').resolves([]);

  return { ...authMocks, countPagesStub, countSessionsStub };
};
