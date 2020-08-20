import React from 'react';

import Flex, { Props as FlexProps } from '../Flex';

export type Props = Omit<FlexProps, 'flexDirection'>;

const FlexColumn = React.forwardRef<HTMLElement, Props>((props, ref) => {
  return <Flex {...props} flexDirection="column" ref={ref} />;
});

export default FlexColumn;
