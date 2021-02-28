import {
  REBROWSE_ADMIN_DTO,
  REBROWSE_ORGANIZATION_DTO,
} from '@rebrowse/testing';
import { client } from 'sdk';
import { SinonSandbox } from 'sinon';

export const mockLandingPage = (sandbox: SinonSandbox) => {
  const retrieveSsoSessionStub = sandbox
    .stub(client, 'retrieve')
    .callsFake(() => {
      return Promise.resolve({
        data: {
          user: REBROWSE_ADMIN_DTO,
          organization: REBROWSE_ORGANIZATION_DTO,
        },
        statusCode: 200,
        headers: new Headers(),
      });
    });

  return { retrieveSsoSessionStub };
};
