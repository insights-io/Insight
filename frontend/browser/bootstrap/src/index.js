/* eslint-disable no-param-reassign */
/* eslint-disable no-underscore-dangle */

((window, doc, scriptTag) => {
  window._i_debug = !1;
  window._i_host = 'insight.com';
  window._i_org = '<ORG>';
  window._i_ns = 'IS';

  const scriptElement = doc.createElement(scriptTag);
  scriptElement.async = true;
  scriptElement.crossOrigin = 'anonymous';
  scriptElement.src = process.env.TRACKING_SCRIPT;
  const firstScript = doc.getElementsByTagName(scriptTag)[0];
  firstScript.parentNode.insertBefore(scriptElement, firstScript);
})(window, document, 'script');
