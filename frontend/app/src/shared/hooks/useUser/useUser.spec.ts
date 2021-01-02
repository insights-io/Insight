import { sandbox } from '@rebrowse/testing';
import { client, INCLUDE_CREDENTIALS } from 'sdk';
import { REBROWSE_ADMIN, REBROWSE_ADMIN_DTO } from '__tests__/data';
import { httpOkResponse, renderHook } from '__tests__/utils';

import { useUser } from './useUser';

describe('useUser', () => {
  test('Should correctly map user', async () => {
    const retrieveUserStub = sandbox
      .stub(client.auth.users, 'me')
      .resolves(httpOkResponse(REBROWSE_ADMIN_DTO));

    const { result, waitForNextUpdate } = renderHook(() =>
      useUser(REBROWSE_ADMIN_DTO)
    );
    expect(result.current.user).toEqual(REBROWSE_ADMIN);

    await waitForNextUpdate();
    sandbox.assert.calledWithExactly(retrieveUserStub, INCLUDE_CREDENTIALS);
    expect(result.current.user).toEqual(REBROWSE_ADMIN);
  });
});
