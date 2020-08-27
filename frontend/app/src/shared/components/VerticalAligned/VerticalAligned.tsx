import React from 'react';

import FlexColumn, { Props as FlexColumnProps } from '../FlexColumn';

type Props = Omit<FlexColumnProps, 'justifyContent'>;

const VerticalAligned = React.forwardRef<HTMLElement, Props>((props, ref) => {
  return <FlexColumn {...props} justifyContent="center" ref={ref} />;
});

export default VerticalAligned;
