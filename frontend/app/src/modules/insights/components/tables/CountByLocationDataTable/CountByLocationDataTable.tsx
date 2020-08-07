import React, { useMemo } from 'react';
import {
  Unstable_StatefulDataTable as DataTable,
  NumericalColumn,
  CategoricalColumn,
} from 'baseui/data-table';
import { CountByLocation } from 'modules/insights/components//charts/CountByLocationMapChart/utils';
import { Block } from 'baseui/block';

const COLUMNS = [
  CategoricalColumn({
    title: 'Continent name',
    mapDataToValue: (data: CountByLocation[number]) =>
      data['location.continentName'],
  }),
  CategoricalColumn({
    title: 'Country name',
    mapDataToValue: (data: CountByLocation[number]) =>
      data['location.countryName'],
  }),
  NumericalColumn({
    title: 'Count',
    mapDataToValue: (data: CountByLocation[number]) => data.count,
  }),
];

type Props = {
  data: CountByLocation;
  height: string;
};

const CountByLocationDataTable = ({ data: countByLocation, height }: Props) => {
  const rows = useMemo(() => countByLocation.map((row) => ({ data: row })), [
    countByLocation,
  ]);

  return (
    <Block height={height}>
      <DataTable
        columns={COLUMNS}
        rows={rows}
        $style={{ width: '100%', overflow: 'auto' }}
        rowHeight={50}
      />
    </Block>
  );
};

export default CountByLocationDataTable;
