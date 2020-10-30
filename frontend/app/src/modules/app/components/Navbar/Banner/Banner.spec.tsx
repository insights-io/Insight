import React from 'react';
import { sandbox } from '@insight/testing';
import userEvent from '@testing-library/user-event';
import { render } from 'test/utils';
import { waitFor } from '@testing-library/react';
import {
  LOGIN_PAGE,
  ORGANIZATION_SETTINGS_MEMBERS_PAGE,
} from 'shared/constants/routes';
import { INSIGHT_ADMIN } from 'test/data';

import { Base, NamelessUserAndOrganization } from './Banner.stories';

describe('<NavbarBanner />', () => {
  it('User should see organization name & user full name & be able to navigate to "Members" section', async () => {
    const { getByText, findByText, push } = render(<Base />);
    userEvent.click(getByText('Insight'));

    await findByText(INSIGHT_ADMIN.email);

    userEvent.click(getByText('Members'));

    await waitFor(() => {
      sandbox.assert.calledWithExactly(
        push,
        ORGANIZATION_SETTINGS_MEMBERS_PAGE,
        ORGANIZATION_SETTINGS_MEMBERS_PAGE,
        { shallow: undefined, locale: undefined }
      );
    });
  });

  it('User should see organization name mock & be able to navigate to sign out', async () => {
    const logoutStub = NamelessUserAndOrganization.story.setupMocks(sandbox);
    const { getByText, findAllByText, findByText, replace } = render(
      <NamelessUserAndOrganization />
    );
    userEvent.click(getByText('My Organization'));

    await findByText('My Account');
    expect((await findAllByText(INSIGHT_ADMIN.email)).length).toEqual(3);

    userEvent.click(getByText('Sign Out'));

    sandbox.assert.calledWithExactly(logoutStub);

    await waitFor(() => {
      sandbox.assert.calledWithExactly(replace, LOGIN_PAGE);
    });
  });
});
