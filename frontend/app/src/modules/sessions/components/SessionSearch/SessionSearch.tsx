import React, { useState } from 'react';
import { Button, SIZE, SHAPE } from 'baseui/button';
import { Filter } from 'baseui/icon';
import { Block } from 'baseui/block';
import { useStyletron } from 'baseui';
import { createBorderRadius } from 'shared/styles/input';

import DateSearch from './DateSearch';
import SessionFilters from './SessionFilters';
import { SessionFilter } from './SessionFilters/utils';

export type Props = {
  onDateRangeChange: (params: {
    from: Date | undefined;
    to: Date | undefined;
  }) => void;
  onValidFiltersUpdate: (filters: SessionFilter[]) => void;
  validFilters: SessionFilter[];
};

const SessionSearch = ({
  onDateRangeChange,
  validFilters,
  onValidFiltersUpdate,
}: Props) => {
  const [_css, theme] = useStyletron();
  const [showFilters, setShowFilters] = useState(false);

  return (
    <Block
      backgroundColor={theme.colors.white}
      padding={theme.sizing.scale400}
      $style={createBorderRadius(theme)}
    >
      <Block display="flex">
        <DateSearch theme={theme} onDateRangeChange={onDateRangeChange} />

        <Button
          size={SIZE.compact}
          $style={{ marginLeft: theme.sizing.scale600, minWidth: '150px' }}
          onClick={() => setShowFilters((prev) => !prev)}
          shape={SHAPE.pill}
        >
          {validFilters.length} Filters
          <Filter
            overrides={{
              Svg: { style: { marginLeft: theme.sizing.scale600 } },
            }}
          />
        </Button>
      </Block>
      {showFilters && (
        <Block marginTop={theme.sizing.scale600}>
          <SessionFilters onValidFiltersUpdate={onValidFiltersUpdate} />
        </Block>
      )}
    </Block>
  );
};

export default SessionSearch;
