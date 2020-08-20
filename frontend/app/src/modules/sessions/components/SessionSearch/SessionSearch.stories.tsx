import React from 'react';
import { action } from '@storybook/addon-actions';

import SessionSearch, { Props } from './SessionSearch';

export default {
  title: 'sessions/components/SessionSearch',
};

export const Base = (props: Partial<Props>) => {
  return (
    <SessionSearch onDateRangeChange={action('onDateRangeChange')} {...props} />
  );
};
