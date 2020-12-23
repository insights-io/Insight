import type { Path } from './types';

export const joinSegments = (segments: string[]) => {
  return `/${segments.join('/')}`;
};

export const joinPath = (path: Path) => {
  return joinSegments(path.map((pathPart) => pathPart.segment));
};
