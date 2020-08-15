import React from 'react';
import { render, RenderableComponent } from 'test/utils';
import { sandbox } from '@insight/testing';
import userEvent from '@testing-library/user-event';
import { StoryConfiguration } from '@insight/storybook';
import { waitFor } from '@testing-library/react';

import {
  Base,
  WithInvalidCodeError,
  WithMissingVerificationIdError,
  WithExpiredVerificationError,
} from './VerificationPage.stories';

describe('<VerificationPage />', () => {
  const renderVerificationPage = <Props, T, S extends StoryConfiguration<T>>(
    component: RenderableComponent<Props, T, S>
  ) => {
    const result = render(component);

    const codeInput = result.container.querySelector(
      'input[aria-label="Please enter your pin code"]'
    ) as HTMLInputElement;

    const submitButton = result.getByText('Submit');

    return { submitButton, codeInput, ...result };
  };

  it('should redirect to dest on success', async () => {
    Base.story.setupMocks(sandbox);
    const { codeInput, submitButton, replace } = renderVerificationPage(
      <Base />
    );

    userEvent.type(codeInput, '123456');
    userEvent.click(submitButton);

    await waitFor(() => {
      sandbox.assert.calledWithExactly(replace, '/');
    });
  });

  it('Should handle invalid code error', async () => {
    WithInvalidCodeError.story.setupMocks(sandbox);
    const { codeInput, submitButton, findByText } = renderVerificationPage(
      <WithInvalidCodeError />
    );

    userEvent.type(codeInput, '123456');
    userEvent.click(submitButton);

    await findByText('Invalid code');
  });

  it('Should handle missing verification id error', async () => {
    WithExpiredVerificationError.story.setupMocks(sandbox);
    const { codeInput, submitButton, replace } = renderVerificationPage(
      <WithExpiredVerificationError />
    );

    userEvent.type(codeInput, '123456');
    userEvent.click(submitButton);

    await waitFor(() => {
      sandbox.assert.calledWithExactly(replace, '/login?dest=%2F');
    });
  });

  it('Should handle expired verification session error', async () => {
    WithMissingVerificationIdError.story.setupMocks(sandbox);
    const { codeInput, submitButton, replace } = renderVerificationPage(
      <WithMissingVerificationIdError />
    );

    userEvent.type(codeInput, '123456');
    userEvent.click(submitButton);

    await waitFor(() => {
      sandbox.assert.calledWithExactly(replace, '/login?dest=%2F');
    });
  });
});
