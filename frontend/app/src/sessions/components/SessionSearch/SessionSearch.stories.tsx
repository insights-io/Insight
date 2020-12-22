import React, { useState } from 'react';
import { action } from '@storybook/addon-actions';
import type { Meta } from '@storybook/react';

import { SessionSearch, Props } from './SessionSearch';
import { SessionFilter } from './SessionFilters/utils';

export default {
  title: 'sessions/components/SessionSearch',
  component: SessionSearch,
} as Meta;

const useFilters = () => {
  const [filters, setFilters] = useState<SessionFilter[]>([]);
  return { filters, setFilters };
};

export const Base = (props: Partial<Props>) => {
  return (
    <SessionSearch
      {...useFilters()}
      onDateRangeChange={action('onDateRangeChange')}
      {...props}
    />
  );
};
