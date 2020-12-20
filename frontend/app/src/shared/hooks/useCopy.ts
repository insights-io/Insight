import { useCallback, useEffect, useRef, useState } from 'react';
import copyToClipboard from 'copy-to-clipboard';

type Options = {
  timeout?: number;
};

export const useCopy = (text = '', { timeout = 1500 }: Options = {}) => {
  const timeoutId = useRef<number>();
  const [isCopied, setIsCopied] = useState(false);

  const copy = useCallback(() => {
    copyToClipboard(text);
    setIsCopied(true);

    clearTimeout(timeoutId.current);
    timeoutId.current = window.setTimeout(() => {
      setIsCopied(false);
    }, timeout);
  }, [text, timeout]);

  useEffect(() => {
    return () => {
      clearTimeout(timeoutId.current);
    };
  }, []);

  return { isCopied, copy };
};
