/* eslint-disable @typescript-eslint/camelcase */
/* eslint-disable no-underscore-dangle */
window._i_debug = !1;
window._i_host = 'insight.com';
window._i_script = 'cdn.insight.com/s/is.js';
window._i_org = '<ORG>';
window._i_ns = 'IS';

((win, doc, namespace, scriptTag) => {
  if (namespace in win) {
    const { console } = win;
    if (console && console.log) {
      console.log('Insight namespace conflict. Please set window["_i_ns"].');
    }
    return;
  }

  const scriptElement = doc.createElement(scriptTag);
  scriptElement.async = true;
  scriptElement.crossOrigin = 'anonymous';
  scriptElement.src = `https://${window._i_script}`;
  const firstScript = doc.getElementsByTagName(scriptTag)[0];
  firstScript.parentNode.insertBefore(scriptElement, firstScript);
})(window, document, window._i_ns, 'script');
