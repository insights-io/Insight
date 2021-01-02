import { mockApiError } from '@rebrowse/storybook';
import { sandbox } from '@rebrowse/testing';
import { client, INCLUDE_CREDENTIALS } from 'sdk';
import { renderHook } from '__tests__/utils';

import { usePasswordPolicy } from './usePasswordPolicy';

describe('usePasswordPolicy', () => {
  test('Should handle missing password policy', async () => {
    const retrieveOrganizationStub = sandbox
      .stub(client.auth.organizations.passwordPolicy, 'retrieve')
      .rejects(
        mockApiError({
          statusCode: 404,
          message: 'Not Found',
          reason: 'Not Found',
        })
      );

    const { result, waitForNextUpdate } = renderHook(() =>
      usePasswordPolicy(undefined)
    );
    expect(result.current.passwordPolicy).toEqual(undefined);

    await waitForNextUpdate();
    sandbox.assert.calledWithExactly(
      retrieveOrganizationStub,
      INCLUDE_CREDENTIALS
    );
    expect(result.current.passwordPolicy).toEqual(undefined);
  });
});
