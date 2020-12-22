import React, { useState } from 'react';
import { SIZE } from 'baseui/button';
import { Filter } from 'baseui/icon';
import { Block } from 'baseui/block';
import { useStyletron } from 'baseui';
import { Flex, Button, expandBorderRadius } from '@rebrowse/elements';

import DateSearch from './DateSearch';
import SessionFilters from './SessionFilters';
import { SessionFilter } from './SessionFilters/utils';

export type Props = {
  onDateRangeChange: (params: {
    from: Date | undefined;
    to: Date | undefined;
  }) => void;
  setFilters: (filters: SessionFilter[]) => void;
  filters: SessionFilter[];
};

export const SessionSearch = ({
  onDateRangeChange,
  filters,
  setFilters,
}: Props) => {
  const [_css, theme] = useStyletron();
  const [showFilters, setShowFilters] = useState(false);

  return (
    <Block
      backgroundColor={theme.colors.white}
      padding={theme.sizing.scale400}
      $style={expandBorderRadius(theme.sizing.scale100)}
    >
      <Flex>
        <DateSearch theme={theme} onDateRangeChange={onDateRangeChange} />

        <Button
          size={SIZE.compact}
          $style={{ marginLeft: theme.sizing.scale600, minWidth: '150px' }}
          onClick={() => setShowFilters((prev) => !prev)}
        >
          {filters.length} Filters
          <Filter
            overrides={{
              Svg: { style: { marginLeft: theme.sizing.scale600 } },
            }}
          />
        </Button>
      </Flex>
      <Block
        marginTop={theme.sizing.scale600}
        display={showFilters ? undefined : 'none'}
      >
        <SessionFilters onChange={setFilters} />
      </Block>
    </Block>
  );
};
