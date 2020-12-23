import type { Path } from 'shared/components/Breadcrums';

export const joinSegments = (segments: string[]) => {
  return `/${segments.join('/')}`;
};

export const joinPath = (path: Path) => {
  return joinSegments(path.map((pathPart) => pathPart.segment));
};
