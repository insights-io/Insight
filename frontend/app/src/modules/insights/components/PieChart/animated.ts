import type { PieArcDatum } from '@visx/shape/lib/shapes/Pie';

export type AnimatedStyles = {
  startAngle: number;
  endAngle: number;
  opacity: number;
};

export type Transition<Datum> = {
  item: PieArcDatum<Datum>;
  props: AnimatedStyles;
  key: string;
};

export const fromLeaveTransition = ({ endAngle }: PieArcDatum<unknown>) => ({
  // enter from 360° if end angle is > 180°
  startAngle: endAngle > Math.PI ? 2 * Math.PI : 0,
  endAngle: endAngle > Math.PI ? 2 * Math.PI : 0,
  opacity: 0,
});

export const enterUpdateTransition = ({
  startAngle,
  endAngle,
}: PieArcDatum<unknown>) => ({
  startAngle,
  endAngle,
  opacity: 1,
});
