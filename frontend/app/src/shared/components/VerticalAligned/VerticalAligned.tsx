import React from 'react';

import FlexColumn, { Props as FlexColumnProps } from '../FlexColumn';

type Props = Omit<FlexColumnProps, 'justifyContent'>;

const VerticalAligned = (props: Props) => {
  return <FlexColumn {...props} justifyContent="center" />;
};

export default VerticalAligned;
