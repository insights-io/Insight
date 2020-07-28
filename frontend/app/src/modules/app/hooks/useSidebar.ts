import { useState, useCallback } from 'react';
import { SIDEBAR_WIDTH, EXPANDED_SIDEBAR_WIDTH } from 'shared/theme';

const useSidebar = () => {
  const [expanded, setExpanded] = useState(false);
  const width = expanded ? EXPANDED_SIDEBAR_WIDTH : SIDEBAR_WIDTH;

  const onCollapseItemClick = useCallback(() => {
    setExpanded((prev) => !prev);
  }, []);

  return { width, expanded, onCollapseItemClick };
};

export default useSidebar;
