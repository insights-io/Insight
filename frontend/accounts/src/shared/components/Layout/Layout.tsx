import React from 'react';
import { useStyletron } from 'baseui';
import { Block } from 'baseui/block';
import { Topbar } from 'shared/components/Topbar';
import { FlexColumn } from '@rebrowse/elements';
import { helpBaseURL } from 'shared/config';

type Props = {
  children: (params: ReturnType<typeof useStyletron>) => JSX.Element;
};

export const Layout = ({ children }: Props) => {
  const styletron = useStyletron();

  return (
    <FlexColumn height="100%">
      <Topbar helpBaseURL={helpBaseURL} />
      <Block
        height="100%"
        display="flex"
        flexDirection="column"
        justifyContent="center"
        padding={styletron[1].sizing.scale600}
      >
        <Block
          width="100%"
          maxWidth="480px"
          marginLeft="auto"
          marginRight="auto"
        >
          {children(styletron)}
        </Block>
      </Block>
    </FlexColumn>
  );
};
