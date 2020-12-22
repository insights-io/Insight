import React, { useCallback, useMemo, useRef } from 'react';
import { scaleLinear, scaleTime } from '@visx/scale';
import { Line, LinePath } from '@visx/shape';
import { curveLinear } from '@visx/curve';
import { useTooltip, defaultStyles, TooltipWithBounds } from '@visx/tooltip';
import { localPoint } from '@visx/event';
import { bisector } from 'd3-array';
import { Group } from '@visx/group';
import { GridRows, GridColumns } from '@visx/grid';
import { AxisBottom } from '@visx/axis';
import { format } from 'date-fns';
import { ResponsiveChart } from 'insights/components/ResponsiveChart';
import { getMinMax } from 'shared/utils/math';

export const accentColorDark = '#75daad';
const tooltipStyles = {
  ...defaultStyles,
  background: '#6086d6',
  color: '#e0e0e0',
};

type Props<Datum> = {
  data: Datum[];
  width: number;
  height: number;
  getY: (d: Datum) => number;
  getX: (d: Datum) => number;
};

export const LineChart = <Datum,>({
  data,
  width,
  height,
  getX,
  getY,
}: Props<Datum>) => {
  const svgRef = useRef<SVGSVGElement>(null);
  const bisectDate = useMemo(() => bisector<Datum, number>(getX).left, [getX]);
  const {
    tooltipOpen,
    tooltipData,
    tooltipLeft,
    tooltipTop,
    showTooltip,
    hideTooltip,
  } = useTooltip<Datum>();

  const lineHeight = height - 22.5;

  const [{ min: minX, max: maxX }, { min: minY, max: maxY }] = useMemo(
    () => getMinMax(data, getX, getY),
    [data, getX, getY]
  );

  const xScale = useMemo(() => {
    return scaleTime({
      domain: [minX, maxX],
      range: [0, width - 5],
      round: true,
    });
  }, [width, minX, maxX]);

  const yScale = useMemo(() => {
    return scaleLinear({
      domain: [maxY, minY],
      range: [0, lineHeight],
      round: true,
    });
  }, [lineHeight, maxY, minY]);

  const handleTooltip = useCallback(
    (
      event: React.TouchEvent<SVGSVGElement> | React.MouseEvent<SVGSVGElement>
    ) => {
      const { x: xPoint } = localPoint(svgRef.current || event, event) || {
        x: 0,
        y: 0,
      };
      const x0 = xScale.invert(xPoint);
      const index = bisectDate(data, x0.valueOf(), 1);

      const d0 = data[index - 1];
      const d1 = data[index];
      let d = d0;
      if (d1 && getX(d1)) {
        d = x0.valueOf() - getX(d0) > getX(d1) - x0.valueOf() ? d1 : d0;
      }

      showTooltip({
        tooltipData: d,
        tooltipLeft: xPoint,
        tooltipTop: yScale(getY(d)),
      });
    },
    [showTooltip, data, getX, getY, xScale, yScale, bisectDate]
  );

  return (
    <>
      <svg
        ref={svgRef}
        width={width}
        height={height}
        onTouchStart={handleTooltip}
        onTouchMove={handleTooltip}
        onMouseMove={handleTooltip}
        onMouseLeave={() => hideTooltip()}
      >
        <Group>
          <GridRows
            scale={yScale}
            width={width}
            height={lineHeight}
            stroke="#e0e0e0"
            opacity={0.1}
            strokeWidth={0.5}
          />

          <GridColumns
            scale={xScale}
            width={width}
            height={lineHeight}
            stroke="#e0e0e0"
            opacity={0.1}
            strokeWidth={0.5}
          />

          <LinePath
            data={data}
            curve={curveLinear}
            x={(value) => xScale(getX(value))}
            y={(value) => yScale(getY(value))}
            stroke="#21cb78"
            strokeWidth={1.5}
          />

          <AxisBottom
            top={lineHeight}
            scale={xScale}
            stroke="#e0e0e0"
            tickStroke="#e0e0e0"
            tickFormat={(d) => format(d as Date, 'MMM d')}
            tickLabelProps={() => ({
              fill: '#e0e0e0',
              fontSize: 11,
              textAnchor: 'middle',
            })}
          />
        </Group>

        {tooltipData && tooltipLeft !== undefined && tooltipTop !== undefined && (
          <g>
            <Line
              from={{ x: tooltipLeft, y: 0 }}
              to={{ x: tooltipLeft, y: lineHeight }}
              stroke="#e0e0e0"
              strokeWidth={1}
              style={{ pointerEvents: 'none' }}
              strokeDasharray="2,2"
            />
          </g>
        )}
      </svg>
      {tooltipOpen &&
        tooltipData &&
        tooltipLeft !== undefined &&
        tooltipTop !== undefined && (
          <TooltipWithBounds
            left={tooltipLeft}
            top={tooltipTop}
            style={tooltipStyles}
          >
            <div>Count: {getY(tooltipData)}</div>
          </TooltipWithBounds>
        )}
    </>
  );
};

export const ResponsiveLineChart = <Datum,>(
  props: Omit<Props<Datum>, 'width' | 'height'>
) => {
  return (
    <ResponsiveChart>
      {({ width, height }) => (
        <LineChart width={width} height={height} {...props} />
      )}
    </ResponsiveChart>
  );
};
