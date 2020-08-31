import React from 'react';
import { action } from '@storybook/addon-actions';
import { Block } from 'baseui/block';
import { SignUpRequestDTO } from '@insight/types';
import { mockApiError } from '@insight/storybook';

import SignUpForm, { Props } from './SignUpForm';

export default {
  title: 'components/SignUpForm',
};

type StoryProps = Pick<Props, 'onSubmit'>;

const baseProps: StoryProps = {
  onSubmit: (data: SignUpRequestDTO) => {
    return new Promise<unknown>((resolve) => {
      setTimeout(() => resolve(action('onSubmit')(data)), 500);
    });
  },
};

export const Base = (storyProps: StoryProps) => {
  return (
    <Block width="100%" maxWidth="720px" marginLeft="auto" marginRight="auto">
      <SignUpForm {...baseProps} {...storyProps} />
    </Block>
  );
};

const baseErrorProps: StoryProps = {
  onSubmit: (_data: SignUpRequestDTO) => {
    return new Promise<unknown>((_resolve, reject) => {
      setTimeout(
        () =>
          reject(
            mockApiError({
              statusCode: 400,
              reason: 'Bad Request',
              message: 'Something went wrong',
            })
          ),
        500
      );
    });
  },
};

export const WithError = (storyProps: StoryProps) => {
  return (
    <Block width="100%" maxWidth="720px" marginLeft="auto" marginRight="auto">
      <SignUpForm {...baseErrorProps} {...storyProps} />
    </Block>
  );
};
