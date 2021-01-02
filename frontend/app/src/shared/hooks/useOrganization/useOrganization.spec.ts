import { sandbox } from '@rebrowse/testing';
import { client, INCLUDE_CREDENTIALS } from 'sdk';
import {
  REBROWSE_ORGANIZATION,
  REBROWSE_ORGANIZATION_DTO,
} from '__tests__/data';
import { httpOkResponse, renderHook } from '__tests__/utils';

import { useOrganization } from './useOrganization';

describe('useOrganization', () => {
  test('Should correctly map organization', async () => {
    const retrieveOrganizationStub = sandbox
      .stub(client.auth.organizations, 'get')
      .resolves(httpOkResponse(REBROWSE_ORGANIZATION_DTO));

    const { result, waitForNextUpdate } = renderHook(() =>
      useOrganization(REBROWSE_ORGANIZATION_DTO)
    );
    expect(result.current.organization).toEqual(REBROWSE_ORGANIZATION);

    await waitForNextUpdate();
    sandbox.assert.calledWithExactly(
      retrieveOrganizationStub,
      INCLUDE_CREDENTIALS
    );
    expect(result.current.organization).toEqual(REBROWSE_ORGANIZATION);
  });
});
