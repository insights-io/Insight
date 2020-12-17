import { ParentSize } from '@visx/responsive';
import { Block } from 'baseui/block';
import React from 'react';

type ParentSizeState = {
  width: number;
  height: number;
  top: number;
  left: number;
};

type Props = {
  children: (
    args: {
      ref: HTMLDivElement | null;
      resize: (state: ParentSizeState) => void;
    } & ParentSizeState
  ) => React.ReactNode;
};

export const ResponsiveChartContainer = ({ children }: Props) => {
  return (
    <ParentSize debounceTime={10}>
      {(args) => (
        <Block
          width={`${args.width}px`}
          height={`${args.height}px`}
          position="relative"
        >
          {children(args)}
        </Block>
      )}
    </ParentSize>
  );
};
