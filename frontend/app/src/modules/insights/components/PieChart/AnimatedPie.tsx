import React from 'react';
import type { ProvidedProps, PieArcDatum } from '@visx/shape/lib/shapes/Pie';
import { animated, useTransition, interpolate } from 'react-spring';

import {
  enterUpdateTransition,
  fromLeaveTransition,
  AnimatedStyles,
  Transition,
} from './animated';

type AnimatedPieProps<Datum> = ProvidedProps<Datum> & {
  getKey: (d: PieArcDatum<Datum>) => string;
  getColor: (d: PieArcDatum<Datum>) => string;
  onClickDatum: (d: PieArcDatum<Datum>) => void;
  onMouseMove?: (
    event: React.MouseEvent<SVGPathElement, MouseEvent>,
    d: Datum
  ) => void;
  onMouseLeave?: (event: React.MouseEvent<SVGPathElement, MouseEvent>) => void;
  onTouchMove?: (event: React.TouchEvent<SVGPathElement>, d: Datum) => void;
  onTouchStart?: (event: React.TouchEvent<SVGPathElement>, d: Datum) => void;
};

export function AnimatedPie<Datum>({
  arcs,
  path,
  getKey,
  getColor,
  onClickDatum,
  onMouseMove,
  onMouseLeave,
  onTouchMove,
  onTouchStart,
}: AnimatedPieProps<Datum>) {
  const transitions = useTransition<PieArcDatum<Datum>, AnimatedStyles>(
    arcs,
    getKey,
    // eslint-disable-next-line @typescript-eslint/ban-ts-comment
    // @ts-ignore react-spring doesn't like this overload
    {
      from: fromLeaveTransition,
      enter: enterUpdateTransition,
      update: enterUpdateTransition,
      leave: fromLeaveTransition,
    }
  );

  return (
    <>
      {transitions.map((transition: Transition<Datum>) => {
        const { item: arc, props, key } = transition;
        if (!arc) {
          return null;
        }
        const [centroidX, centroidY] = path.centroid(arc);
        const hasSpaceForLabel = arc.endAngle - arc.startAngle >= 0.1;

        return (
          <g key={key}>
            <animated.path
              d={interpolate(
                [props.startAngle, props.endAngle],
                (startAngle, endAngle) => path({ ...arc, startAngle, endAngle })
              )}
              fill={getColor(arc)}
              onClick={(event) => {
                onClickDatum(arc);
                event.stopPropagation();
                event.preventDefault();
              }}
              onTouchStart={(event) => {
                onClickDatum(arc);
                onTouchStart?.(event, arc.data);
              }}
              onMouseMove={(event) => onMouseMove?.(event, arc.data)}
              onMouseLeave={onMouseLeave}
              onTouchMove={(event) => onTouchMove?.(event, arc.data)}
            />
            {hasSpaceForLabel && (
              <animated.g style={{ opacity: props.opacity }}>
                <text
                  fill="white"
                  x={centroidX}
                  y={centroidY}
                  dy=".33em"
                  fontSize={9}
                  textAnchor="middle"
                  pointerEvents="none"
                >
                  {getKey(arc)}
                </text>
              </animated.g>
            )}
          </g>
        );
      })}
    </>
  );
}
