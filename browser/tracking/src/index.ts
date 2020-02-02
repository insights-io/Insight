import Context from 'context';
import EventQueue from 'queue';
import { EventType } from 'event';

(location => {
  let { href: lastLocation } = location;
  const context = new Context();
  const eventQueue = new EventQueue(context);

  const onNavigationChange = () => {
    const { href: currentLocation } = location;
    if (lastLocation !== currentLocation) {
      lastLocation = currentLocation;
      eventQueue.enqueue(EventType.NAVIGATE, [currentLocation, document.title]);
    }
  };

  window.addEventListener('popstate', onNavigationChange);
  // eslint-disable-next-line no-restricted-globals
})(location);
