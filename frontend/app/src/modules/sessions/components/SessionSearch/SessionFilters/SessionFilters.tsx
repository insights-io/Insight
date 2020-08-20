import React, { useEffect } from 'react';
import { useStyletron } from 'baseui';

import { SessionFilter } from './utils';
import useSessionFilters from './useSessionFilters';
import SessionFilterRow from './SessionFilterRow';

type Props = {
  onChange: (filters: SessionFilter[]) => void;
};

const SessionFilters = ({ onChange }: Props) => {
  const [css, theme] = useStyletron();
  const {
    filters,
    onPlus,
    onDelete,
    onUpdateFilter,
    validFilters,
  } = useSessionFilters();

  useEffect(() => {
    onChange(validFilters);
  }, [validFilters, onChange]);

  return (
    <ul className={css({ padding: 0, margin: 0 })}>
      {filters.map((filter, index) => {
        return (
          <SessionFilterRow
            index={index}
            key={filter.id}
            filter={filter}
            onPlus={onPlus}
            onDelete={onDelete}
            onUpdateFilter={onUpdateFilter}
            theme={theme}
          />
        );
      })}
    </ul>
  );
};

export default React.memo(SessionFilters);
