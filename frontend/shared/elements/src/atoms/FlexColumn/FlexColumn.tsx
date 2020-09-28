import React, { forwardRef } from 'react';

import { Flex, FlexProps } from '../Flex';

export type Props = Omit<FlexProps, 'flexDirection'>;

export const FlexColumn = forwardRef<HTMLElement, Props>((props, ref) => {
  return <Flex {...props} flexDirection="column" ref={ref} />;
});
