import React from 'react';
import { fullHeightDecorator } from '@rebrowse/storybook';
import type { Meta } from '@storybook/react';

import { ErrorPage } from './ErrorPage';

export default {
  title: 'app/pages/ErrorPage',
  component: ErrorPage,
  decorators: [fullHeightDecorator],
} as Meta;

export const Base = () => {
  return <ErrorPage statusCode={500} />;
};
