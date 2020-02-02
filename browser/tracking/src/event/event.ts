export const enum EventType {
  NAVIGATE = 0,
}

export type BrowserEventArgument = string | number;

export type BrowserEvent = {
  when: number;
  kind: EventType;
  args: BrowserEventArgument[];
};
