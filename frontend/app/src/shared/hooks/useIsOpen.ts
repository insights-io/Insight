import { useCallback, useState } from 'react';

export const useIsOpen = () => {
  const [isOpen, setIsOpen] = useState(false);
  const close = useCallback(() => setIsOpen(false), []);
  const open = useCallback(() => setIsOpen(true), []);

  return { isOpen, close, open };
};
