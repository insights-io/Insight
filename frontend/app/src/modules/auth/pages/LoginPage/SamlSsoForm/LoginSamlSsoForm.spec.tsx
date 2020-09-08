import { sandbox } from '@insight/testing';
import { waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import React from 'react';
import { render } from 'test/utils';
import * as windowUtils from 'shared/utils/window';

import { Base, SsoNotEnabled } from './LoginSamlSsoForm.stories';

describe('<LoginSamlSsoForm />', () => {
  it('Should redirect to correct location', async () => {
    Base.story.setupMocks(sandbox);
    const locationAssignStub = sandbox.stub(windowUtils, 'locationAssign');
    const { getByPlaceholderText, getByText, findByText } = render(<Base />);
    const input = getByPlaceholderText('user@company.com');
    const submitButton = getByText('Sign in');

    expect(submitButton).toBeDisabled();
    await userEvent.type(input, 'matej');

    await findByText('Please enter a valid email address');
    expect(submitButton).toBeDisabled();

    await userEvent.type(input, 'matej.snuderl@snuderls.eu');
    expect(submitButton).not.toBeDisabled();

    userEvent.click(submitButton);

    await waitFor(() => {
      sandbox.assert.calledWithExactly(
        locationAssignStub,
        'http://localhost:8080/v1/sso/saml/signin?redirect=%2F&email=matejmatej.snuderl%40snuderls.eu'
      );
    });
  });

  it('Should display error on SSO not enabled', async () => {
    SsoNotEnabled.story.setupMocks(sandbox);
    const { getByPlaceholderText, getByText, findByText } = render(
      <SsoNotEnabled />
    );
    const input = getByPlaceholderText('user@company.com');
    const submitButton = getByText('Sign in');

    await userEvent.type(input, 'matej.snuderl@snuderls.eu');
    userEvent.click(submitButton);

    await findByText('That email or domain isn’t registered for SSO.');
  });
});
