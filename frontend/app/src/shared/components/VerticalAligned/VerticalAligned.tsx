import React from 'react';

import Flex, { Props as FlexProps } from '../Flex';

type Props = Omit<FlexProps, 'justifyContent' | 'flexDirection'>;

const VerticalAligned = (props: Props) => {
  return <Flex {...props} justifyContent="center" flexDirection="column" />;
};

export default VerticalAligned;
