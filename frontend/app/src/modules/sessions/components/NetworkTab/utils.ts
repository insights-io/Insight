import {
  BrowserResourcePerformanceEventDTO,
  BrowserXhrEventDTO,
} from '@insight/types';

export type NetworkTabEventDTO = Omit<BrowserXhrEventDTO, 'e'> &
  Omit<BrowserResourcePerformanceEventDTO, 'e'>;
