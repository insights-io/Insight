import { ParentSize } from '@visx/responsive';
import { Block, BlockProps } from 'baseui/block';
import React from 'react';

type ParentSizeState = {
  width: number;
  height: number;
  top: number;
  left: number;
};

export type ResponsiveChartProps = Omit<
  BlockProps,
  'width' | 'height' | 'position'
>;

type Props = ResponsiveChartProps & {
  children: (
    args: {
      ref: HTMLDivElement | null;
      resize: (state: ParentSizeState) => void;
    } & ParentSizeState
  ) => React.ReactNode;
};

export const ResponsiveChart = ({ children, ...rest }: Props) => {
  return (
    <ParentSize debounceTime={10}>
      {(args) => (
        <Block
          width={`${args.width}px`}
          height={`${args.height}px`}
          position="relative"
          {...rest}
        >
          {children(args)}
        </Block>
      )}
    </ParentSize>
  );
};
