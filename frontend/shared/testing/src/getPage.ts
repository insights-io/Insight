import { join } from 'path';

import { getPage as originalGetPage } from 'next-page-tester';

export const getPage: typeof originalGetPage = ({
  nonIsolatedModules = [],
  ...options
}) => {
  return originalGetPage({
    useDocument: true,
    nonIsolatedModules: [
      ...nonIsolatedModules,
      'styletron-react',
      join(process.cwd(), 'src/sdk'),
    ],
    ...options,
  });
};
