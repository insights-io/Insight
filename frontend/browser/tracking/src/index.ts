import Context from 'context';
import EventQueue from 'queue';
import {
  encodeEventTarget,
  // BrowserEventArguments,
  dedupMouseEventSimple,
  //  mouseEventSimpleArgs,
  mouseEventWithTargetArgs,
} from 'event';
import { logger } from 'logger';
import Backend from 'backend';
import { CreatePageResponse, EventType } from '@rebrowse/types';
import Identity from 'identity';
import { MILLIS_IN_SECOND } from 'time';
import type { RebrowseWindow, Enqueue } from 'types';
import { instrumentGlobals } from 'instrument';

declare global {
  // eslint-disable-next-line @typescript-eslint/no-empty-interface
  interface Window extends RebrowseWindow {}
}

((window, location, compiledTs) => {
  let { href: lastHref } = location;
  const context = new Context();
  const eventQueue = new EventQueue(context);
  const { _i_org: organizationId, _i_host: host } = window;
  const identity = Identity.initFromCookie(host, organizationId);
  const backend = new Backend(
    `${process.env.BEACON_API_BASE_URL}`,
    `${process.env.SESSION_API_BASE_URL}`,
    organizationId,
    context
  );

  const observer = new PerformanceObserver((performanceEntryList) => {
    performanceEntryList.getEntries().forEach((entry) => {
      if (entry instanceof PerformanceResourceTiming) {
        const { name: url, initiatorType, nextHopProtocol } = entry;
        if (initiatorType === 'fetch' || initiatorType === 'xmlhttprequest') {
          const events = eventQueue.events();
          for (let i = events.length - 1; i >= 0; i--) {
            const event = events[i];
            if (
              event.e === EventType.XHR &&
              event.a[1] === url &&
              event.a[4] === initiatorType
            ) {
              event.a.push(nextHopProtocol);
              break;
            }
          }
        }
        eventQueue.enqueue(EventType.RESOURCE_PERFORMANCE, [
          url,
          entry.startTime,
          entry.duration,
          initiatorType,
          nextHopProtocol,
        ]);
      }
    });
  });

  const entryTypes = ['resource'];
  observer.observe({ entryTypes });

  const enqueue: Enqueue = (eventType, args, eventName, event) => {
    eventQueue.enqueue(eventType, args, event);
    if (process.env.NODE_ENV !== 'production' && eventType !== EventType.LOG) {
      logger.debug(eventName, args);
    }
  };

  /*
  const onResize = () => {
    const { innerWidth, innerHeight } = window;
    const args = [innerWidth, innerHeight];
    enqueue(EventType.RESIZE, args, '[resize]');
  };
  */

  const onNavigationChange = (event: PopStateEvent) => {
    const { href: currentHref } = location;
    if (lastHref !== currentHref) {
      lastHref = currentHref;
      const args = [currentHref, document.title];
      enqueue(EventType.NAVIGATE, args, '[navigate]', event);
    }
  };

  /*
  const onMouseDown = (event: MouseEvent) => {
    enqueue(EventType.MOUSEDOWN, mouseEventSimpleArgs(event), '[mousedown]');
  };
  */

  /*
  const onMouseUp = (event: MouseEvent) => {
    enqueue(EventType.MOUSEUP, mouseEventSimpleArgs(event), '[mouseup]');
  };
  */

  const {
    //   onMouseEvent: onMouseMove,
    clearMouseEvent: onMouseMoveClear,
  } = dedupMouseEventSimple(
    (event: MouseEvent, clientX: number, clientY: number) => {
      const args = [clientX, clientY, ...encodeEventTarget(event)];
      enqueue(EventType.MOUSEMOVE, args, '[mousemove]', event);
    }
  );

  const onClick = (event: MouseEvent) => {
    const args = mouseEventWithTargetArgs(event);
    enqueue(EventType.MOUSEMOVE, args, '[mousemove]', event);
  };

  const onLoad = (event: Event) => {
    enqueue(EventType.LOAD, [lastHref], '[resize]', event);
  };

  const onError = (event: ErrorEvent) => {
    enqueue(
      EventType.ERROR,
      [event.error.message, event.error.name, event.error.stack],
      '[error]',
      event
    );
  };

  window.addEventListener('popstate', onNavigationChange);
  // window.addEventListener('resize', onResize);
  window.addEventListener('load', onLoad);
  window.addEventListener('click', onClick);
  window.addEventListener('error', onError);
  instrumentGlobals(enqueue);
  // window.addEventListener('mousemove', onMouseMove);
  // window.addEventListener('mousedown', onMouseDown);
  // window.addEventListener('mouseup', onMouseUp);

  const onUnload = (event: Event) => {
    const args = [lastHref];
    eventQueue.enqueue(EventType.UNLOAD, args, event);
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
    const UPLOAD_INTERVAL_MILLIS = MILLIS_IN_SECOND * 10; // every 10 seconds
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
      href: lastHref,
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
