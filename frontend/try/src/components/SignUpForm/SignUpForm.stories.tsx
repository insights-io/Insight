import React from 'react';
import { action } from '@storybook/addon-actions';
import { Block } from 'baseui/block';

import SignUpForm, { Props, FormData } from './SignUpForm';

export default {
  title: 'SignUpForm',
};

type StoryProps = Pick<Props, 'onSubmit'>;

const baseProps: StoryProps = {
  onSubmit: (data: FormData) => {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    return new Promise<any>((resolve) => {
      setTimeout(() => resolve(action('onSubmit')(data)), 200);
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
