/* eslint-disable no-underscore-dangle */
import Context from 'context/Context';
import { BrowserEvent, BrowserEventArgument } from 'event';
import { EventType } from '@rebrowse/types';

class EventQueue {
  private readonly _context: Context;

  private _rawQueue: BrowserEvent[];

  constructor(context: Context) {
    this._context = context;
    this._rawQueue = [];
  }

  public enqueue = (
    kind: EventType,
    args: BrowserEventArgument[],
    event?: Event
  ) => {
    this.enqueueAt(event?.timeStamp || this._context.now(), kind, args);
  };

  public drainEvents = (): BrowserEvent[] => {
    return this._rawQueue.splice(0, this._rawQueue.length);
  };

  public events = (): BrowserEvent[] => {
    return this._rawQueue;
  };

  private enqueueAt = (
    when: number,
    kind: EventType,
    args: BrowserEventArgument[]
  ) => {
    this._rawQueue.push({ t: when, e: kind, a: args });
  };
}

export default EventQueue;
