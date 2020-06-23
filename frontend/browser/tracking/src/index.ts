/* eslint-disable no-underscore-dangle */
import Context from 'context';
import EventQueue from 'queue';
import {
  EventType,
  encodeEventTarget,
  BrowserEventArguments,
  dedupMouseEventSimple,
  mouseEventSimpleArgs,
  mouseEventWithTargetArgs,
} from 'event';
import { logger } from 'logger';
import Backend from 'backend';
import { CreatePageResponse } from '@insight/types';
import Identity from 'identity';
import { MILLIS_IN_SECOND } from 'time';
import type { InsightWindow } from 'types';

declare global {
  // eslint-disable-next-line @typescript-eslint/no-empty-interface
  interface Window extends InsightWindow {}
}

((window, location, compiledTs) => {
  let { href: lastLocation } = location;
  const context = new Context();
  const eventQueue = new EventQueue(context);
  const { _i_org: organizationId, _i_host: host } = window;
  const identity = Identity.initFromCookie(host, organizationId);
  const backend = new Backend(
    `${process.env.BEACON_API_BASE_URL}`,
    `${process.env.SESSION_API_BASE_URL}`,
    organizationId
  );
  const UPLOAD_INTERVAL_MILLIS = MILLIS_IN_SECOND * 10;

  const observer = new PerformanceObserver((performanceEntryList) => {
    performanceEntryList.getEntries().forEach((entry) => {
      eventQueue.enqueue(EventType.PERFORMANCE, [
        entry.name,
        entry.entryType,
        entry.startTime,
        entry.duration,
      ]);
    });
  });

  const entryTypes = ['navigation', 'resource', 'measure', 'mark'];
  observer.observe({ entryTypes });

  const enqueue = (
    eventType: EventType,
    args: BrowserEventArguments,
    eventName: string
  ) => {
    eventQueue.enqueue(eventType, args);
    if (process.env.NODE_ENV !== 'production') {
      logger.debug(eventName, args);
    }
  };

  const onResize = () => {
    const { innerWidth, innerHeight } = window;
    const args = [innerWidth, innerHeight];
    enqueue(EventType.RESIZE, args, '[resize]');
  };

  const onNavigationChange = () => {
    const { href: currentLocation } = location;
    if (lastLocation !== currentLocation) {
      lastLocation = currentLocation;
      const args = [currentLocation, document.title];
      enqueue(EventType.NAVIGATE, args, '[navigate]');
    }
  };

  const onMouseDown = (event: MouseEvent) => {
    enqueue(EventType.MOUSEDOWN, mouseEventSimpleArgs(event), '[mousedown]');
  };

  const onMouseUp = (event: MouseEvent) => {
    enqueue(EventType.MOUSEUP, mouseEventSimpleArgs(event), '[mouseup]');
  };

  const {
    onMouseEvent: onMouseMove,
    clearMouseEvent: onMouseMoveClear,
  } = dedupMouseEventSimple(
    (event: MouseEvent, clientX: number, clientY: number) => {
      const args = [clientX, clientY, ...encodeEventTarget(event)];
      enqueue(EventType.MOUSEMOVE, args, '[mousemove]');
    }
  );

  const onClick = (event: MouseEvent) => {
    const args = mouseEventWithTargetArgs(event);
    enqueue(EventType.MOUSEMOVE, args, '[mousemove]');
  };

  const onLoad = () => {
    enqueue(EventType.LOAD, [lastLocation], '[resize]');
  };

  window.addEventListener('popstate', onNavigationChange);
  window.addEventListener('resize', onResize);
  window.addEventListener('load', onLoad);
  window.addEventListener('click', onClick);
  window.addEventListener('mousemove', onMouseMove);
  window.addEventListener('mousedown', onMouseDown);
  window.addEventListener('mouseup', onMouseUp);

  const onUnload = () => {
    const args = [lastLocation];
    eventQueue.enqueue(EventType.UNLOAD, args);
    backend.sendBeacon(eventQueue.events()).catch((error) => {
      if (process.env.NODE_ENV !== 'production') {
        logger.error('Something went wrong while sending beacon', error);
      }
    });
  };

  const startBeaconing = (pageResponse: CreatePageResponse) => {
    const { data: pageIdentity } = pageResponse;
    backend.connect(pageIdentity);
    identity.connect(pageIdentity);
    window.addEventListener('unload', onUnload);
    setInterval(() => {
      const events = eventQueue.drainEvents();
      if (events.length > 0) {
        backend.sendEvents(events);
        onMouseMoveClear();
        if (process.env.NODE_ENV !== 'production') {
          logger.debug('[onUploadInterval]', [events.length]);
        }
      }
    }, UPLOAD_INTERVAL_MILLIS);
  };

  backend
    .page({
      organizationId,
      deviceId: identity.deviceId(),
      compiledTs,
      doctype: '<!DOCTYPE html>',
      height: window.innerHeight,
      width: window.innerWidth,
      screenHeight: window.screen.height,
      screenWidth: window.screen.width,
      referrer: document.referrer,
      url: lastLocation,
    })
    .then(startBeaconing)
    .catch((error) => {
      // TODO: have some error reporting & retrying
      if (process.env.NODE_ENV !== 'production') {
        logger.error('Something went wrong while creating page', error);
      }
    });

  // eslint-disable-next-line no-restricted-globals
})(window, location, (process.env.COMPILED_TS as unknown) as number);
