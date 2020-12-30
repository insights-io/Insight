import { sandbox } from '@rebrowse/testing';
import { AuthApi } from 'api';
import { REBROWSE_ADMIN, REBROWSE_ADMIN_DTO } from '__tests__/data';
import { httpOkResponse, renderHook } from '__tests__/utils';

import { useUser } from './useUser';

describe('useUser', () => {
  test('Should work as expected', async () => {
    const retrieveUserStub = sandbox
      .stub(AuthApi.user, 'me')
      .resolves(httpOkResponse(REBROWSE_ADMIN_DTO));

    const { result, waitForNextUpdate } = renderHook(() =>
      useUser(REBROWSE_ADMIN_DTO)
    );
    expect(result.current.user).toEqual(REBROWSE_ADMIN);

    await waitForNextUpdate();
    sandbox.assert.calledWithExactly(retrieveUserStub);
    expect(result.current.user).toEqual(REBROWSE_ADMIN);
  });
});
