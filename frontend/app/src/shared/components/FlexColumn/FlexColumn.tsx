import React from 'react';

import Flex, { Props as FlexProps } from '../Flex';

export type Props = Omit<FlexProps, 'flexDirection'>;

const FlexColumn = (props: Props) => {
  return <Flex {...props} flexDirection="column" />;
};

export default FlexColumn;
