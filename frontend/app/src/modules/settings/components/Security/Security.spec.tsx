import { sandbox } from '@insight/testing';
import { waitFor } from '@testing-library/react';
import React from 'react';
import { render } from 'test/utils';

import { TfaDisabled, TfaEnabled, WithError } from './Security.stories';

describe('<Security />', () => {
  it('Should render enabled checkbox', async () => {
    const { listSetups } = TfaEnabled.story.setupMocks(sandbox);
    const { container } = render(<TfaEnabled />);
    const checkbox = container.querySelector('input');

    await waitFor(() => {
      sandbox.assert.calledWithExactly(listSetups);
    });

    expect(checkbox).toHaveAttribute('aria-checked', 'true');
  });

  it('Should render checkbox', async () => {
    const { listSetups } = TfaDisabled.story.setupMocks(sandbox);
    const { container } = render(<TfaDisabled />);
    const checkbox = container.querySelector('input');

    await waitFor(() => {
      sandbox.assert.calledWithExactly(listSetups);
    });

    expect(checkbox).toHaveAttribute('aria-checked', 'false');
  });

  it('Should render errror', async () => {
    const { listSetups } = WithError.story.setupMocks(sandbox);
    const { container, findByText } = render(<WithError />);
    const checkbox = container.querySelector('input');

    await waitFor(() => {
      sandbox.assert.calledWithExactly(listSetups);
    });

    await findByText('Internal Server Error');

    expect(checkbox).toHaveAttribute('aria-checked', 'false');
    expect(checkbox).toHaveAttribute('disabled');
  });
});
