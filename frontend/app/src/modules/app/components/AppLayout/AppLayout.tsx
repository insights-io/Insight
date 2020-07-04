import React from 'react';
import { Block, BlockProps } from 'baseui/block';
import { $StyleProp } from 'styletron-react';

import Sidebar from '../Sidebar';

type Props = {
  children: React.ReactNode;
  overrides?: {
    Root?: {
      style?: $StyleProp<BlockProps>;
    };
    MainContent?: {
      style?: $StyleProp<BlockProps>;
    };
  };
};

const AppLayout = ({ children, overrides }: Props) => {
  return (
    <Block height="100%" display="flex" $style={overrides?.Root?.style}>
      <Sidebar />
      <Block
        height="100%"
        display="flex"
        flexDirection="column"
        flex="1"
        width="calc(100% - 48px)"
        $style={{ boxSizing: 'border-box', ...overrides?.MainContent?.style }}
        overflow="hidden"
      >
        {children}
      </Block>
    </Block>
  );
};

export default AppLayout;
