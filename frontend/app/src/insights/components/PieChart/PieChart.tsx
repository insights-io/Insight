import React, { useCallback, useRef, useState } from 'react';
import { Group } from '@visx/group';
import { Pie } from '@visx/shape';
import { defaultStyles, TooltipWithBounds, useTooltip } from '@visx/tooltip';
import { localPoint } from '@visx/event';
import { ResponsiveChart } from 'insights/components/ResponsiveChart';

import { AnimatedPie } from './AnimatedPie';

const tooltipStyles = {
  ...defaultStyles,
  background: '#6086d6',
  color: '#e0e0e0',
};

type Props<Datum> = {
  width: number;
  height: number;
  data: Datum[];
  getLabel: (d: Datum) => string;
  getPieValue: (d: Datum) => number;
  getColor: (d: Datum) => string;
  donutThickness?: number;
  getTooltipLabel?: (d: Datum) => React.ReactNode;
};

export const PieChart = <Datum,>({
  width,
  height,
  donutThickness = 50,
  data,
  getLabel,
  getPieValue,
  getColor,
  getTooltipLabel = (d: Datum) => (
    <>
      {getLabel(d)}: {getPieValue(d)}
    </>
  ),
}: Props<Datum>) => {
  const [selectedLabel, setSelectedLabel] = useState<string>();
  const radius = Math.min(width, height) / 2;
  const centerY = height / 2;
  const centerX = width / 2;
  const svgRef = useRef<SVGSVGElement>(null);

  const dispalyedData = selectedLabel
    ? data.filter((d) => getLabel(d) === selectedLabel)
    : data;

  const {
    tooltipOpen,
    tooltipData,
    tooltipLeft,
    tooltipTop,
    showTooltip,
    hideTooltip,
  } = useTooltip<Datum>();

  const handleTooltip = useCallback(
    (
      event:
        | React.TouchEvent<SVGPathElement>
        | React.MouseEvent<SVGPathElement>,
      tooltipData: Datum
    ) => {
      const { x: tooltipLeft, y: tooltipTop } = localPoint(
        svgRef.current || event,
        event
      ) || { x: 0, y: 0 };

      showTooltip({ tooltipData, tooltipLeft, tooltipTop });
    },
    [showTooltip]
  );

  return (
    <>
      <svg
        width={width}
        height={height}
        ref={svgRef}
        onClick={() => setSelectedLabel(undefined)}
      >
        <Group top={centerY} left={centerX} style={{ cursor: 'pointer' }}>
          <Pie
            data={dispalyedData}
            pieValue={getPieValue}
            outerRadius={radius}
            innerRadius={radius - donutThickness}
            cornerRadius={3}
            padAngle={0.005}
          >
            {(pie) => (
              <AnimatedPie<Datum>
                {...pie}
                onMouseLeave={hideTooltip}
                onTouchStart={handleTooltip}
                onTouchMove={handleTooltip}
                onMouseMove={handleTooltip}
                getKey={(arc) => getLabel(arc.data)}
                onClickDatum={({ data }) => {
                  const label = getLabel(data);
                  setSelectedLabel(selectedLabel === label ? undefined : label);
                }}
                getColor={(arc) => getColor(arc.data)}
              />
            )}
          </Pie>
        </Group>
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
            {getTooltipLabel(tooltipData)}
          </TooltipWithBounds>
        )}
    </>
  );
};

export const ResponsivePieChart = <Datum,>(
  props: Omit<Props<Datum>, 'width' | 'height'>
) => {
  return (
    <ResponsiveChart>
      {({ width, height }) => (
        <PieChart width={width} height={height} {...props} />
      )}
    </ResponsiveChart>
  );
};
