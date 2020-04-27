import React from 'react';
import { useStyletron } from 'baseui';
import { Block, BlockProps } from 'baseui/block';

type Props = BlockProps;

const Divider = (props: Props) => {
  const [_css, theme] = useStyletron();
  return (
    <Block
      height="1px"
      marginTop={theme.sizing.scale800}
      marginBottom={theme.sizing.scale800}
      backgroundColor={theme.colors.primary100}
      {...props}
    />
  );
};

export default Divider;
