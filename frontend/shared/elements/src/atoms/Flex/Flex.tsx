import React, { forwardRef } from 'react';
import { Block, BlockProps } from 'baseui/block';

export type Props = Omit<BlockProps, 'display'>;

export const Flex = forwardRef<HTMLElement, Props>((props, ref) => {
  return <Block {...props} display="flex" ref={ref} />;
});
