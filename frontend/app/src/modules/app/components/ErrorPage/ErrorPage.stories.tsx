import React from 'react';
import { fullHeightDecorator } from '@insight/storybook';

import ErrorPage from './ErrorPage';

export default {
  title: 'app/ErrorPage',
  decorators: [fullHeightDecorator],
};

export const Base = () => {
  return <ErrorPage statusCode={500} />;
};
