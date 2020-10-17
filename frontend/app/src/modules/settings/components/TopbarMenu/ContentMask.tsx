import React from 'react';
import { Block } from 'baseui/block';
import * as zIndex from 'shared/constants/zIndex';

type Props = {
  active: boolean;
  onClick: () => void;
};

export const ContentMask = ({ active, onClick }: Props) => {
  return (
    <Block
      backgroundColor="rgba(0, 0, 0, 0.35)"
      position="absolute"
      top={0}
      bottom={0}
      left={0}
      right={0}
      overrides={{
        Block: {
          style: {
            opacity: active ? 1 : 0,
            height: active ? undefined : 0,
            transition: 'opacity 0.2s ease-in-out',
            zIndex: zIndex.CONTENT_MASK,
          },
          props: { onClick },
        },
      }}
    />
  );
};
