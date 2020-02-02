/* eslint-disable no-underscore-dangle */
import Context from 'context/Context';
import { EventType, BrowserEvent, BrowserEventArgument } from 'event';

class EventQueue {
  private readonly _context: Context;

  private _rawQueue: BrowserEvent[];

  constructor(context: Context) {
    this._context = context;
    this._rawQueue = [];
  }

  public enqueue = (kind: EventType, args: BrowserEventArgument[]) => {
    this.enqueueAt(this._context.now(), kind, args);
  };

  private enqueueAt = (
    when: number,
    kind: EventType,
    args: BrowserEventArgument[]
  ) => {
    this._rawQueue.push({ when, kind, args });
  };
}

export default EventQueue;
