import React, { forwardRef } from 'react';

import { FlexColumn, FlexColumnProps } from '../FlexColumn';

export type Props = Omit<FlexColumnProps, 'justifyContent'>;

export const VerticalAligned = forwardRef<HTMLElement, Props>((props, ref) => {
  return <FlexColumn {...props} justifyContent="center" ref={ref} />;
});
