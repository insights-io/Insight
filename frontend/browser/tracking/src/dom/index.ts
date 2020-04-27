export const enum NodeType {
  HTML = 1,
  TEXT = 3,
}

export const isTextNode = (target: EventTarget): target is Element => {
  return (
    ((target as unknown) as { nodeType: NodeType }).nodeType === NodeType.TEXT
  );
};

export const isHtmlElement = (target: EventTarget): target is HTMLElement => {
  return (
    ((target as unknown) as { nodeType: NodeType }).nodeType === NodeType.HTML
  );
};

type EncodedTagAndAttributes = (string | null)[];

const extractTextContent = (element: Element): string => {
  const { textContent } = element;
  if (!textContent) return '';
  const { length } = textContent;
  if (length > 16e6) {
    // eslint-disable-next-line no-console
    console.warn('Ignoring huge text node', { length });
    return '';
  }
  return textContent;
};

export const encodeTarget = (target: EventTarget): EncodedTagAndAttributes => {
  if (isTextNode(target)) {
    return [`<${target.nodeName}`, extractTextContent(target)];
  }
  if (isHtmlElement(target)) {
    const values = [`<${target.nodeName}`] as (string | null)[];
    const pushAttributes = (name: string, value: string | null) => {
      values.push(`:${name}`);
      values.push(value);
    };

    if (target.getAttributeNames) {
      const attrs = target.getAttributeNames();
      for (let i = 0; i < attrs.length; i++) {
        const attributeName = attrs[i];
        const attributeValue = target.getAttribute(attributeName);
        pushAttributes(attributeName, attributeValue);
      }
    } else {
      for (let i = 0; i < target.attributes.length; i++) {
        const attribute = target.attributes[i];
        pushAttributes(attribute.name, attribute.value);
      }
    }
    return values;
  }

  if (process.env.NODE_ENV !== 'production') {
    // eslint-disable-next-line no-console
    console.debug('Unknown element type', target);
  }
  return [];
};
