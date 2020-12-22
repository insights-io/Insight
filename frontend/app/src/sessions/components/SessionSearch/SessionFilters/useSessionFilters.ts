import { useCallback, useState, useMemo } from 'react';

import { SessionFilter, generateNewFilter } from './utils';

const useSessionFilters = () => {
  const [filters, setFilters] = useState<SessionFilter[]>([
    generateNewFilter(),
  ]);

  const onPlus = useCallback(() => {
    setFilters((prev) => [...prev, generateNewFilter()]);
  }, []);

  const onDelete = useCallback((id: string) => {
    setFilters((prev) =>
      prev.length === 1
        ? [generateNewFilter()]
        : prev.filter((f) => f.id !== id)
    );
  }, []);

  const onUpdateFilter = useCallback((filter: SessionFilter) => {
    setFilters((prev) => prev.map((f) => (f.id === filter.id ? filter : f)));
  }, []);

  const validFilters = useMemo(
    () => filters.filter((f) => f.key !== undefined && f.value !== ''),
    [filters]
  );

  return { filters, validFilters, onPlus, onDelete, onUpdateFilter };
};

export default useSessionFilters;
