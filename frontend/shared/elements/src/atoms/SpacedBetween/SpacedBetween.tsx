import React, { forwardRef } from 'react';

import { Flex, FlexProps } from '../Flex';

export type Props = Omit<FlexProps, 'justifyContent'>;

export const SpacedBetween = forwardRef<HTMLElement, Props>((props, ref) => {
  return <Flex {...props} justifyContent="space-between" ref={ref} />;
});
