import React from 'react';
import { Block, BlockProps } from 'baseui/block';

export type Props = Omit<BlockProps, 'display'>;

const Flex = (props: Props) => {
  return <Block {...props} display="flex" />;
};

export default Flex;
