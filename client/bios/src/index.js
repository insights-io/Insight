/* eslint-disable @typescript-eslint/no-non-null-assertion */
/* eslint-disable func-names */
/* eslint-disable @typescript-eslint/camelcase */
/* eslint-disable no-underscore-dangle */
window._is_debug = false;
window._is_host = 'insight.com';
window._is_script = 'cdn.insight.com/s/is.js';
window._is_org = '<ORG>';
window._is_namespace = 'IS';

((win, doc, namespace) => {
  if (namespace in win) {
    if (win.console && win.console.log) {
      win.console.log(
        'Insight namespace conflict. Please set window["_is_namespace"].'
      );
    }
    return;
  }

  const scriptElement = doc.createElement('script');
  scriptElement.async = true;
  scriptElement.crossOrigin = 'anonymous';
  scriptElement.src = `https://${window._is_script}`;
  const firstScript = doc.getElementsByTagName('script')[0];
  firstScript.parentNode.insertBefore(scriptElement, firstScript);
})(window, document, window._is_namespace);
