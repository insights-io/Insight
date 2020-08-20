import React from 'react';
import { Block, BlockProps } from 'baseui/block';

export type Props = Omit<BlockProps, 'display'>;

const Flex = React.forwardRef<HTMLElement, Props>((props, ref) => {
  return <Block {...props} display="flex" ref={ref} />;
});

export default Flex;
