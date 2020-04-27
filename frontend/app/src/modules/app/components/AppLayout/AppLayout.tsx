import React from 'react';
import { Block } from 'baseui/block';

import Navbar from '../Navbar';

type Props = {
  children: React.ReactNode;
};

const AppLayout = ({ children }: Props) => {
  return (
    <Block height="100%" display="flex" flexDirection="column">
      <Navbar />
      <Block height="100%">{children}</Block>
    </Block>
  );
};

export default AppLayout;
