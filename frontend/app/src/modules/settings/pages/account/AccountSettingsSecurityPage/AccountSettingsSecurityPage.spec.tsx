import React from 'react';
import { sandbox } from '@rebrowse/testing';
import { BoundFunction, GetByText, waitFor } from '@testing-library/react';
import { render } from 'test/utils';

import {
  TfaDisabled,
  TfaEnabled,
  WithError,
} from './AccountSettingsSecurityPage.stories';

const getCheckboxByText = (
  getByText: BoundFunction<GetByText>,
  text: string
) => {
  return getByText(text).parentElement?.querySelector(
    'input'
  ) as HTMLInputElement;
};

describe('<AccountSettingsSecurityPage />', () => {
  it('Should render enabled checkbox', async () => {
    const { listSetups } = TfaEnabled.story.setupMocks(sandbox);
    const { getByText } = render(<TfaEnabled />);

    const authenticatorAppCheckbox = getCheckboxByText(
      getByText,
      'Authy / Google Authenticator'
    );
    const textMessageCheckbox = getCheckboxByText(getByText, 'Text message');

    await waitFor(() => {
      sandbox.assert.calledWithExactly(listSetups);
    });

    expect(authenticatorAppCheckbox).toHaveAttribute('aria-checked', 'true');
    expect(textMessageCheckbox).toHaveAttribute('aria-checked', 'false');
  });

  it('Should render checkbox disabled', async () => {
    const { listSetups } = TfaDisabled.story.setupMocks(sandbox);
    const { getByText } = render(<TfaDisabled />);

    const authenticatorAppCheckbox = getCheckboxByText(
      getByText,
      'Authy / Google Authenticator'
    );
    const textMessageCheckbox = getCheckboxByText(getByText, 'Text message');

    await waitFor(() => {
      sandbox.assert.calledWithExactly(listSetups);
    });

    expect(authenticatorAppCheckbox).toHaveAttribute('aria-checked', 'false');
    expect(textMessageCheckbox).toHaveAttribute('aria-checked', 'false');
  });

  it('Should render error and checkboxes disabled', async () => {
    const { listSetups } = WithError.story.setupMocks(sandbox);
    const { getByText, findByText } = render(<WithError />);

    const authenticatorAppCheckbox = getCheckboxByText(
      getByText,
      'Authy / Google Authenticator'
    );
    const textMessageCheckbox = getCheckboxByText(getByText, 'Text message');

    await waitFor(() => {
      sandbox.assert.calledWithExactly(listSetups);
    });

    await findByText('Internal Server Error');

    expect(authenticatorAppCheckbox).toHaveAttribute('disabled');
    expect(authenticatorAppCheckbox).toHaveAttribute('aria-checked', 'false');

    expect(textMessageCheckbox).toHaveAttribute('disabled');
    expect(textMessageCheckbox).toHaveAttribute('aria-checked', 'false');
  });
});
