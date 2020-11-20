import {
  LogLevel,
  BrowserLogEventDTO,
  BrowserErrorEventDTO,
} from '@rebrowse/types';
import React from 'react';
import { DeleteAlt, Alert, IconProps } from 'baseui/icon';

export type ConsoleEventDTO = BrowserLogEventDTO | BrowserErrorEventDTO;

type ConsoleEventStyling = { backgroundColor: string; color: string };

const LEVEL_COLOR_MAPPINGS: Record<LogLevel, ConsoleEventStyling> = {
  warn: { backgroundColor: '#e65100', color: '#ffc107' },
  error: { backgroundColor: '#7f0000', color: '#DB4437' },
  debug: { backgroundColor: 'transparent', color: '#1e88e5' },
  log: { backgroundColor: 'transparent', color: '#000' },
  info: { backgroundColor: 'transparent', color: '#000' },
};

const LEVEL_ICON_MAPPINGS: Record<LogLevel, React.FC<IconProps> | null> = {
  error: DeleteAlt,
  warn: Alert,
  debug: null,
  log: null,
  info: null,
};

export const isErrorEvent = (
  event: ConsoleEventDTO
): event is BrowserErrorEventDTO => {
  return (event as BrowserErrorEventDTO).stack !== undefined;
};

export const getEventStyling = (
  event: ConsoleEventDTO
): ConsoleEventStyling => {
  const logLevel = isErrorEvent(event) ? 'error' : event.level;
  return LEVEL_COLOR_MAPPINGS[logLevel];
};

export const getEventIcon = (
  event: ConsoleEventDTO
): React.FC<IconProps> | null => {
  const logLevel = isErrorEvent(event) ? 'error' : event.level;
  return LEVEL_ICON_MAPPINGS[logLevel];
};

export const eventMatchesText = (e: ConsoleEventDTO, filterText: string) => {
  const eventArguments = isErrorEvent(e) ? [e.message, e.stack] : e.arguments;
  return eventArguments.join(' ').includes(filterText);
};
