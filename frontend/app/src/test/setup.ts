/* eslint-disable no-console */
import '@testing-library/jest-dom/extend-expect';

const originalConsoleWarn = console.warn;

console.warn = (...args: unknown[]) => {
  const msg = args.join(' ');

  [
    'Mixing shorthand and longhand properties within the same style object is unsupported with atomic rendering',
  ].forEach((pattern) => {
    if (String(msg).match(pattern)) {
      throw new Error(msg);
    }
  });

  originalConsoleWarn(...args);
};
