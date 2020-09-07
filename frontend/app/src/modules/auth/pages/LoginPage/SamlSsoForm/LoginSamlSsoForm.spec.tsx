import { sandbox } from '@insight/testing';
import userEvent from '@testing-library/user-event';
import React from 'react';
import * as windowUtils from 'shared/utils/window';
import { render } from 'test/utils';

import { Base } from './LoginSamlSsoForm.stories';

describe('<LoginSamlSsoForm />', () => {
  it('Should redirect to correct location', async () => {
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
    sandbox.assert.calledWithExactly(
      locationAssignStub,
      'http://localhost:8080/v1/sso/saml/signin?redirect=%2F&email=matejmatej.snuderl%40snuderls.eu'
    );
  });
});
