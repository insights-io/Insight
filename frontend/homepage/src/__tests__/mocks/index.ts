import { sdk } from 'api';
import { SinonSandbox } from 'sinon';

export const mockLandingPage = (
  sandbox: SinonSandbox,
  { loggedIn = true }: { loggedIn?: boolean } = {}
) => {
  const retrieveSsoSessionStub = sandbox
    .stub(sdk, 'retrieve')
    .resolves({ status: loggedIn ? 200 : 204 } as Response);

  return { retrieveSsoSessionStub };
};
