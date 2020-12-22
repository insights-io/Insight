import {
  BrowserResourcePerformanceEventDTO,
  BrowserXhrEventDTO,
} from '@rebrowse/types';

export type NetworkTabEventDTO = Omit<BrowserXhrEventDTO, 'e'> &
  Omit<BrowserResourcePerformanceEventDTO, 'e'>;
