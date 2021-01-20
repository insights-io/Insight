/* eslint-disable @typescript-eslint/no-empty-function */
/* eslint-disable class-methods-use-this */
/* eslint-disable no-console */

import sandbox from './sandbox';

export const setupEnvironment = () => {
  const originalConsoleWarn = console.warn;
  const originalConsoleError = console.error;

  class IntersectionObserver {
    observe() {}
    unobserve() {}
    disconnect() {}
  }

  // Mock IntersectionObserver (Link component relies on it)
  if (!global.IntersectionObserver) {
    // @ts-expect-error missing dom type
    global.IntersectionObserver = IntersectionObserver;
  }

  // Mock window.scrollTo (Link component triggers it)
  // eslint-disable-next-line lodash/prefer-constant
  global.scrollTo = () => null;

  // https://testing-library.com/docs/dom-testing-library/api-helpers#debugging
  if (process.env.CI) {
    process.env.DEBUG_PRINT_LIMIT = '100000';
  }

  // Error: Not implemented: window.scrollTo
  sandbox.stub(window, 'scrollTo');

  console.error = (...args: unknown[]) => {
    const msg = args.join(' ');

    ['Warning: Invalid DOM property'].forEach((pattern) => {
      if (String(msg).match(pattern)) {
        throw new Error(msg);
      }
    });

    originalConsoleError(...args);
  };

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
};
